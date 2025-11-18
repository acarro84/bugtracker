package com.iworks.bugtracker.repository;

import com.iworks.bugtracker.model.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BugReportRepository extends JpaRepository<BugReport, Long>,
        JpaSpecificationExecutor<BugReport> {

    // For now, we rely entirely on JpaSpecificationExecutor
    // via BugReportService.buildAdminSpecification(...) for:
    //  - searchIssues (paginated)
    //  - exportIssues (full list)
    //
    // If we ever need custom JPQL queries again (e.g., performance tuning),
    // we can reintroduce @Query methods with standard String concatenation
    // instead of text blocks to avoid compilation issues.
}
