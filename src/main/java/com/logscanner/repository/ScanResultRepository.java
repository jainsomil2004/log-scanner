package com.logscanner.repository;

import com.logscanner.model.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult,Long> {
    // Get all by severity
    List<ScanResult> findBySeverityOrderByScannedAtDesc(String severity);

    // Get recent errors (last 24 hours)
    List<ScanResult> findByScannedAtAfterOrderByScannedAtDesc(
            LocalDateTime since);

    // Count by severity
    long countBySeverity(String severity);

    // Get latest 10 errors
    List<ScanResult> findTop10ByOrderByScannedAtDesc();

    // Get errors by error type
    List<ScanResult> findByErrorTypeOrderByScannedAtDesc(String errorType);

    // Stats — count by error type
    @Query("SELECT s.errorType, COUNT(s) FROM ScanResult s GROUP BY s.errorType")
    List<Object[]> countGroupByErrorType();

    // Stats — count by severity
    @Query("SELECT s.severity, COUNT(s) FROM ScanResult s GROUP BY s.severity")
    List<Object[]> countGroupBySeverity();

}
