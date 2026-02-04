package com.rabin.backend.repository;

import com.rabin.backend.enums.ReportStatus;
import com.rabin.backend.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportStatus(ReportStatus status);
    List<Report> findByEvent_Id(Long eventId);
    boolean existsByReporter_IdAndEvent_IdAndReportStatus(Long reporterId, Long eventId, ReportStatus status);

    // Count methods for admin dashboard
    long countByReportStatus(ReportStatus status);

    // Delete all reports for an event
    void deleteByEvent_Id(Long eventId);
}
