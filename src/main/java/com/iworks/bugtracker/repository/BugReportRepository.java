package com.iworks.bugtracker.repository;

import com.iworks.bugtracker.model.BugReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BugReportRepository extends JpaRepository<BugReport, Long>, JpaSpecificationExecutor<BugReport> {

    /**
     * Search for issues for the admin view with filters and pagination.
     *
     * @param type      Issue type filter: "Bug", "Feature Request", "Comment", or null for all types.
     * @param resolved  Resolved status filter: true, false, or null for both.
     * @param fromDate  Filter by createdAt >= fromDate (nullable).
     * @param toDate    Filter by createdAt <= toDate (nullable).
     * @param pageable  Pagination information (page number, size).
     * @return          A page of BugReport entities matching the filters.
     */


    /**
     * Same filters as searchForAdmin, but returns all results as a list
     * (used for CSV export; no pagination).
     */

}
