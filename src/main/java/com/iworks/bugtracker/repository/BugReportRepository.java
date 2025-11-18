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
     * Main admin search query.
     *
     * Filters:
     *  - type (Bug / Feature Request / Comment, or null for all)
     *  - resolved (null = all, true = resolved only, false = unresolved only)
     *  - createdAt between fromDate/toDate (nullable)
     *  - includeDeleted: if false, hide logically deleted issues
     *
     * Semantics:
     *  - deleted = TRUE  => logically deleted
     *  - deleted = FALSE => active
     *  - deleted = NULL  => treated as active (for backward compatibility)
     *
     * Ordering:
     *  - unresolved first
     *  - then by createdAt desc
     */
    @Query("""
        select br
        from BugReport br
        where (:type is null or br.type = :type)
          and (:resolved is null or br.resolved = :resolved)
          and (:fromDate is null or br.createdAt >= :fromDate)
          and (:toDate is null or br.createdAt <= :toDate)
          and (:includeDeleted = true or coalesce(br.deleted, false) = false)
        order by case 
                   when br.resolved = false then 0 
                   else 1 
                 end,
                 br.createdAt desc
    """)
    Page<BugReport> searchForAdmin(
            @Param("type") String type,
            @Param("resolved") Boolean resolved,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );

    /**
     * Same filters as searchForAdmin, but returns all rows (no pagination)
     * for CSV export.
     */
    @Query("""
        select br
        from BugReport br
        where (:type is null or br.type = :type)
          and (:resolved is null or br.resolved = :resolved)
          and (:fromDate is null or br.createdAt >= :fromDate)
          and (:toDate is null or br.createdAt <= :toDate)
          and (:includeDeleted = true or coalesce(br.deleted, false) = false)
        order by case 
                   when br.resolved = false then 0 
                   else 1 
                 end,
                 br.createdAt desc
    """)
    List<BugReport> exportForAdmin(
            @Param("type") String type,
            @Param("resolved") Boolean resolved,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("includeDeleted") boolean includeDeleted
    );
}
