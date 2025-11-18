package com.iworks.bugtracker.service;

import com.iworks.bugtracker.model.BugReport;
import com.iworks.bugtracker.repository.BugReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BugReportService {

    private final BugReportRepository bugReportRepository;

    public BugReportService(BugReportRepository bugReportRepository) {
        this.bugReportRepository = bugReportRepository;
    }

    /**
     * Search issues for the admin view with filters + pagination.
     *
     * @param typeFilter     "Bug", "Feature Request", "Comment", or "All"/null for all types.
     * @param resolvedFilter null for all, true for resolved only, false for unresolved only.
     * @param fromDate       filter createdAt >= fromDate (nullable).
     * @param toDate         filter createdAt <= toDate (nullable).
     * @param viewDeleted    false = hide deleted; true = include deleted issues.
     * @param page           0-based page index.
     * @param size           page size (we'll use 10 for the UI, but it's configurable).
     */
    public Page<BugReport> searchIssues(
            String typeFilter,
            Boolean resolvedFilter,
            LocalDate fromDate,
            LocalDate toDate,
            boolean viewDeleted,
            int page,
            int size
    ) {
        String typeParam = normalizeType(typeFilter);
        Boolean resolvedParam = resolvedFilter;

        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        // Inclusive end-of-day
        LocalDateTime toDateTime = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay().minusNanos(1)
                : null;

        // Unresolved first, then newest first
        Sort sort = Sort.by(
                Sort.Order.asc("resolved"),
                Sort.Order.desc("createdAt")
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<BugReport> spec = buildAdminSpecification(
                typeParam,
                resolvedParam,
                fromDateTime,
                toDateTime,
                viewDeleted
        );

        return bugReportRepository.findAll(spec, pageable);
    }

    /**
     * Get a full list of issues matching filters for CSV export (no pagination).
     */
    public List<BugReport> exportIssues(
            String typeFilter,
            Boolean resolvedFilter,
            LocalDate fromDate,
            LocalDate toDate,
            boolean viewDeleted
    ) {
        String typeParam = normalizeType(typeFilter);
        Boolean resolvedParam = resolvedFilter;

        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay().minusNanos(1)
                : null;

        Sort sort = Sort.by(
                Sort.Order.asc("resolved"),
                Sort.Order.desc("createdAt")
        );

        Specification<BugReport> spec = buildAdminSpecification(
                typeParam,
                resolvedParam,
                fromDateTime,
                toDateTime,
                viewDeleted
        );

        return bugReportRepository.findAll(spec, sort);
    }

    /**
     * Apply bulk resolution + logical delete changes coming from the admin UI.
     *
     * The UI sends a list of updates with:
     *  - id
     *  - resolved
     *  - deleted         (true = logically delete)
     *  - resolvedBy
     *  - resolutionDescription
     *
     * Rules:
     *  - You can toggle resolved <-> unresolved.
     *  - When going unresolved, resolution fields are cleared.
     *  - You can ONLY delete an issue that is already resolved.
     *  - We do NOT support "undelete" from here (deleted=false leaves it as-is).
     *
     * If an invalid delete is attempted, we throw IllegalStateException.
     */
    @Transactional
    public void applyBulkResolutionChanges(List<BulkUpdateRequest> updates) {
        LocalDateTime now = LocalDateTime.now();

        for (BulkUpdateRequest update : updates) {
            if (update.getId() == null) {
                continue; // ignore invalid entries
            }

            Optional<BugReport> optional = bugReportRepository.findById(update.getId());
            if (optional.isEmpty()) {
                continue; // ignore missing ids
            }

            BugReport existing = optional.get();
            boolean wasResolved = existing.isResolved();
            boolean willBeResolved = update.isResolved();
            boolean deleteRequested = update.isDeleted();

            // --- Handle resolved/unresolved first ---
            if (willBeResolved) {
                // Mark as resolved
                existing.setResolved(true);
                existing.setResolvedBy(update.getResolvedBy());
                existing.setResolutionDescription(update.getResolutionDescription());

                // Only set resolvedAt when transitioning from unresolved -> resolved
                if (!wasResolved) {
                    existing.setResolvedAt(now);
                }
            } else {
                // Mark as unresolved (always clear resolution info)
                existing.setResolved(false);
                existing.setResolvedBy(null);
                existing.setResolutionDescription(null);
                existing.setResolvedAt(null);
            }

            // --- Handle logical delete ---
            if (deleteRequested) {
                // Business rule: cannot delete if not resolved
                if (!existing.isResolved()) {
                    throw new IllegalStateException("Cannot delete an unresolved issue (id=" + existing.getId() + ")");
                }
                // Only mark deleted; we do not "undelete" here
                if (!existing.isDeleted()) {
                    existing.setDeleted(true);
                    existing.setDeletedAt(now);
                }
            }

            bugReportRepository.save(existing);
        }
    }

    /**
     * Builds a dynamic Specification for the admin filters.
     */
    private Specification<BugReport> buildAdminSpecification(
            String typeParam,
            Boolean resolvedParam,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime,
            boolean viewDeleted
    ) {
        Specification<BugReport> spec = Specification.where(null);

        if (typeParam != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), typeParam));
        }

        if (resolvedParam != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("resolved"), resolvedParam));
        }

        if (fromDateTime != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
        }

        if (toDateTime != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
        }

        // Hide deleted by default; include them only if explicitly requested.
        // Treat NULL as "not deleted" for backward compatibility.
        if (!viewDeleted) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.isFalse(root.get("deleted")),
                            cb.isNull(root.get("deleted"))
                    ));
        }

        return spec;
    }

    /**
     * Normalizes the issue type filter:
     *  - null or "All" (case-insensitive) → null (no filter)
     *  - otherwise returns the original string.
     */
    private String normalizeType(String typeFilter) {
        if (typeFilter == null) {
            return null;
        }
        String trimmed = typeFilter.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if ("all".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    /**
     * DTO used for bulk admin updates of resolution state and logical delete.
     *
     * The controller will map JSON into this type.
     */
    public static class BulkUpdateRequest {
        private Long id;
        private boolean resolved;
        private boolean deleted; // request logical delete
        private String resolvedBy;
        private String resolutionDescription;

        public BulkUpdateRequest() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isResolved() {
            return resolved;
        }

        public void setResolved(boolean resolved) {
            this.resolved = resolved;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
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
    }
}
