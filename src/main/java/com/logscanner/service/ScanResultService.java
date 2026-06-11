package com.logscanner.service;

import com.logscanner.model.ErrorReport;
import com.logscanner.model.ScanResult;
import com.logscanner.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScanResultService {

    private final ScanResultRepository scanResultRepository;

    // Save all errors from a scan to MySQL
    public void saveScanResults(ErrorReport report, boolean emailSent) {
        if (report.getErrors().isEmpty()) return;

        report.getErrors().forEach(error -> {
            ScanResult result = ScanResult.builder()
                    .logFilePath(report.getLogFilePath())
                    .scannedAt(report.getScannedAt())
                    .totalErrors(report.getTotalErrors())
                    .severity(error.getSeverity())
                    .errorType(error.getErrorType())
                    .lineNumber(error.getLineNumber())
                    .errorMessage(error.getErrorMessage())
                    .rootCause(error.getRootCause())
                    .stackTrace(String.join("\n", error.getStackTrace()))
                    .emailSent(emailSent)
                    .emailSentAt(emailSent ? LocalDateTime.now() : null)
                    .build();

            scanResultRepository.save(result);
            log.info("💾 Saved to DB: [{}] {} at line {}",
                    error.getSeverity(),
                    error.getErrorType(),
                    error.getLineNumber());
        });
    }

    // Dashboard stats
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalErrors = scanResultRepository.count();
        long fatalCount  = scanResultRepository.countBySeverity("FATAL");
        long errorCount  = scanResultRepository.countBySeverity("ERROR");

        // Recent errors (last 24 hours)
        List<ScanResult> recent = scanResultRepository
                .findByScannedAtAfterOrderByScannedAtDesc(
                        LocalDateTime.now().minusHours(24));

        // Error type breakdown
        List<Object[]> byType = scanResultRepository.countGroupByErrorType();
        Map<String, Long> errorTypeMap = new HashMap<>();
        byType.forEach(row -> errorTypeMap.put(
                (String) row[0], (Long) row[1]));

        // Severity breakdown
        List<Object[]> bySeverity = scanResultRepository.countGroupBySeverity();
        Map<String, Long> severityMap = new HashMap<>();
        bySeverity.forEach(row -> severityMap.put(
                (String) row[0], (Long) row[1]));

        stats.put("totalErrors",   totalErrors);
        stats.put("fatalCount",    fatalCount);
        stats.put("errorCount",    errorCount);
        stats.put("last24Hours",   recent.size());
        stats.put("byErrorType",   errorTypeMap);
        stats.put("bySeverity",    severityMap);
        stats.put("latestErrors",
                scanResultRepository.findTop10ByOrderByScannedAtDesc());

        return stats;
    }

    public List<ScanResult> getAllErrors() {
        return scanResultRepository.findAll();
    }

    public List<ScanResult> getErrorsBySeverity(String severity) {
        return scanResultRepository
                .findBySeverityOrderByScannedAtDesc(severity);
    }

    public List<ScanResult> getRecentErrors() {
        return scanResultRepository
                .findByScannedAtAfterOrderByScannedAtDesc(
                        LocalDateTime.now().minusHours(24));
    }
}
