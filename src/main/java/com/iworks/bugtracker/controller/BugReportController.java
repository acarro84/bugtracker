package com.iworks.bugtracker.controller;

import com.iworks.bugtracker.model.BugReport;
import com.iworks.bugtracker.repository.BugReportRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
public class BugReportController {

    private final BugReportRepository bugReportRepository;

    // These will be set based on properties bugtracker.upload-dir and bugtracker.report-dir
    private final Path uploadDir;
    private final Path reportsDir;
    private final Path csvFile;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public BugReportController(
            BugReportRepository bugReportRepository,
            @Value("${bugtracker.upload-dir}") String uploadDirProperty,
            @Value("${bugtracker.report-dir}") String reportsDirProperty
    ) {
        this.bugReportRepository = bugReportRepository;

        // Normalize paths (works for both relative and absolute paths)
        this.uploadDir = Paths.get(uploadDirProperty).toAbsolutePath().normalize();
        this.reportsDir = Paths.get(reportsDirProperty).toAbsolutePath().normalize();
        this.csvFile = this.reportsDir.resolve("bug_reports.csv");

        // Ensure directories exist
        try {
            Files.createDirectories(this.uploadDir);
            Files.createDirectories(this.reportsDir);
        } catch (IOException e) {
            // Fail fast if we can't prepare storage directories
            throw new IllegalStateException("Failed to create upload/report directories", e);
        }
    }

    @PostMapping("/bug-report")
    public ResponseEntity<String> handleBugReport(
            @RequestParam("name") @NotBlank @Size(max = 100) String name,
            @RequestParam("email") @NotBlank @Email @Size(max = 150) String email,
            @RequestParam("role") @NotBlank @Size(max = 100) String role,
            @RequestParam("browser") @NotBlank @Size(max = 50) String browser,
            @RequestParam(value = "datetime", required = false) String datetime,
            @RequestParam("type") @NotBlank @Size(max = 50) String type,
            @RequestParam("description") @NotBlank @Size(max = 255) String description,
            @RequestParam(value = "screenshot", required = false) MultipartFile screenshot
    ) {
        // enforce @iworkscorp.com domain
        if (!email.toLowerCase().endsWith("@iworkscorp.com")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email must be an @iworkscorp.com address.");
        }

        BugReport report = new BugReport();
        report.setName(name);
        report.setEmail(email);
        report.setRole(role);
        report.setBrowser(browser);
        report.setType(type);
        report.setDescription(description);

        // optional datetime-local field (yyyy-MM-dd'T'HH:mm)
        if (datetime != null && !datetime.isBlank()) {
            try {
                report.setEventTime(LocalDateTime.parse(datetime));
            } catch (Exception e) {
                // ignore parse errors for now
            }
        }

        // server-side createdAt timestamp
        report.setCreatedAt(LocalDateTime.now());

        // handle optional file upload
        if (screenshot != null && !screenshot.isEmpty()) {
            try {
                String originalFilename =
                        StringUtils.cleanPath(screenshot.getOriginalFilename());
                String filename = System.currentTimeMillis() + "_" + originalFilename;

                Path targetPath = uploadDir.resolve(filename);
                Files.copy(screenshot.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Store the absolute path (or relative, up to you)
                report.setScreenshotPath(targetPath.toString());
            } catch (IOException e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to save screenshot.");
            }
        }

        // save to database first so we get the generated ID
        BugReport saved = bugReportRepository.save(report);

        // append to CSV "spreadsheet" (best-effort; don't fail the request if this fails)
        try {
            appendToCsv(saved);
        } catch (IOException e) {
            System.err.println("Failed to write to CSV: " + e.getMessage());
        }

        return ResponseEntity.ok("Bug report submitted. Thank you!");
    }

    private void appendToCsv(BugReport report) throws IOException {
        boolean fileExists = Files.exists(csvFile);

        try (BufferedWriter writer = Files.newBufferedWriter(
                csvFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            // write header once
            if (!fileExists) {
                writer.write("id,name,email,role,browser,type,description,event_time,created_at,screenshot_path");
                writer.newLine();
            }

            String eventTimeStr = report.getEventTime() != null
                    ? dateTimeFormatter.format(report.getEventTime())
                    : "";

            String createdAtStr = report.getCreatedAt() != null
                    ? dateTimeFormatter.format(report.getCreatedAt())
                    : "";

            writer.write(String.join(",",
                    csv(report.getId()),
                    csv(report.getName()),
                    csv(report.getEmail()),
                    csv(report.getRole()),
                    csv(report.getBrowser()),
                    csv(report.getType()),
                    csv(report.getDescription()),
                    csv(eventTimeStr),
                    csv(createdAtStr),
                    csv(report.getScreenshotPath())
            ));
            writer.newLine();
        }
    }

    // Simple CSV escaping: wrap in quotes and escape internal quotes
    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String s = String.valueOf(value);
        s = s.replace("\"", "\"\""); // escape quotes
        return "\"" + s + "\"";
    }
}
