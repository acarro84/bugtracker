package com.iworks.bugtracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "bug_reports")
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String role;

    @NotBlank
    @Size(max = 50)
    private String browser;

    // Optional – when the user says it happened
    private LocalDateTime eventTime;

    @NotBlank
    @Size(max = 50)
    private String type;  // Bug, Feature Request, Comment

    @NotBlank
    @Size(max = 255)
    private String description;

    @Size(max = 500)
    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- New resolution-related fields ---

    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @Size(max = 100)
    @Column(name = "resolved_by")
    private String resolvedBy;

    @Size(max = 255)
    @Column(name = "resolution_description")
    private String resolutionDescription;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // --- Lifecycle hooks ---

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // --- Getters & setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionDescription() {
        return resolutionDescription;
    }

    public void setResolutionDescription(String resolutionDescription) {
        this.resolutionDescription = resolutionDescription;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    // Optional convenience methods (purely for readability)

    public void markResolved(String resolvedBy, String resolutionDescription, LocalDateTime resolvedAt) {
        this.resolved = true;
        this.resolvedBy = resolvedBy;
        this.resolutionDescription = resolutionDescription;
        this.resolvedAt = resolvedAt;
    }

    public void markUnresolved() {
        this.resolved = false;
        this.resolvedBy = null;
        this.resolutionDescription = null;
        this.resolvedAt = null;
    }
}
