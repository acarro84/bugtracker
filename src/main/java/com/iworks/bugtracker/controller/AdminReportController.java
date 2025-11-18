package com.iworks.bugtracker.controller;

import com.iworks.bugtracker.model.BugReport;
import com.iworks.bugtracker.service.BugReportService;
import com.iworks.bugtracker.service.BugReportService.BulkUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminReportController {

    private final BugReportService bugReportService;

    public AdminReportController(BugReportService bugReportService) {
        this.bugReportService = bugReportService;
    }

    /**
     * GET /api/admin/issues
     *
     * Returns a page of issues for the admin view,
     * filtered by type, resolved state, date range, and deleted state.
     */
    @GetMapping("/issues")
    public Page<BugReport> getIssues(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "resolved", required = false) String resolved,
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "viewDeleted", defaultValue = "false") boolean viewDeleted,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Boolean resolvedFilter = parseResolvedFilter(resolved);
        return bugReportService.searchIssues(
                type,
                resolvedFilter,
                fromDate,
                toDate,
                viewDeleted,
                page,
                size
        );
    }

    /**
     * POST /api/admin/issues/bulk-update
     *
     * Applies bulk changes to resolved/unresolved state and logical delete.
     * The front-end is responsible for:
     *  - Only allowing delete on already-resolved issues in the UI.
     *  - Showing confirmation dialogs as needed.
     *
     * If an invalid delete is attempted (e.g. delete on unresolved issue),
     * the service may throw IllegalStateException and we respond with 400.
     */
    @PostMapping("/issues/bulk-update")
    public ResponseEntity<Void> bulkUpdateIssues(
            @RequestBody List<BulkUpdateRequest> updates
    ) {
        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            bugReportService.applyBulkResolutionChanges(updates);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            // Business rule violation (e.g., delete non-resolved issue)
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/admin/issues/export
     *
     * Exports all issues matching the current filters as a CSV file.
     * Pagination is NOT applied here; the full filtered dataset is exported.
     *
     * Includes logical delete fields in the export.
     */
    @GetMapping("/issues/export")
    public void exportIssuesAsCsv(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "resolved", required = false) String resolved,
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "viewDeleted", defaultValue = "false") boolean viewDeleted,
            HttpServletResponse response
    ) throws IOException {

        Boolean resolvedFilter = parseResolvedFilter(resolved);

        List<BugReport> issues = bugReportService.exportIssues(
                type,
                resolvedFilter,
                fromDate,
                toDate,
                viewDeleted
        );

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"bug_reports_export.csv\""
        );

        try (PrintWriter writer = response.getWriter()) {
            // CSV header – now includes deleted/deleted_at
            writer.println("id,type,name,email,role,browser,description,event_time,created_at," +
                    "resolved,resolved_by,resolution_description,resolved_at,deleted,deleted_at");

            for (BugReport b : issues) {
                writer.println(String.join(",",
                        csv(b.getId()),
                        csv(b.getType()),
                        csv(b.getName()),
                        csv(b.getEmail()),
                        csv(b.getRole()),
                        csv(b.getBrowser()),
                        csv(b.getDescription()),
                        csv(b.getEventTime()),
                        csv(b.getCreatedAt()),
                        csv(b.isResolved()),
                        csv(b.getResolvedBy()),
                        csv(b.getResolutionDescription()),
                        csv(b.getResolvedAt()),
                        csv(b.isDeleted()),
                        csv(b.getDeletedAt())
                ));
            }
            writer.flush();
        }
    }

    /**
     * Helper to parse the "resolved" query parameter:
     *  - null or "all" (case-insensitive) -> null (no filter)
     *  - "true", "resolved" -> true
     *  - "false", "unresolved" -> false
     */
    private Boolean parseResolvedFilter(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().toLowerCase();
        if (v.isEmpty() || "all".equals(v)) {
            return null;
        }
        if ("true".equals(v) || "resolved".equals(v)) {
            return Boolean.TRUE;
        }
        if ("false".equals(v) || "unresolved".equals(v)) {
            return Boolean.FALSE;
        }
        // If it's an unrecognized value, treat as "all"
        return null;
    }

    /**
     * Simple CSV cell formatter:
     *  - null -> empty ""
     *  - wraps in quotes
     *  - escapes internal quotes
     *  - mildly protects against CSV formula injection by prefixing
     *    fields starting with =,+,-,@ with a single quote.
     */
    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String s = String.valueOf(value);

        if (!s.isEmpty()) {
            char first = s.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                s = "'" + s;
            }
        }

        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
