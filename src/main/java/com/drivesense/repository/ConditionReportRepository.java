package com.drivesense.repository;

import com.drivesense.entity.ConditionReport;
import com.drivesense.enums.ReportStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConditionReportRepository extends JpaRepository<ConditionReport, Long> {
    Optional<ConditionReport> findByBookingIdAndStage(Long bookingId, ReportStage stage);
    List<ConditionReport> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
