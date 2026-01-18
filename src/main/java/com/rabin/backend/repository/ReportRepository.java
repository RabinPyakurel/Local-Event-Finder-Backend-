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
}
