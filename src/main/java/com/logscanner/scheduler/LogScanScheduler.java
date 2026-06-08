package com.logscanner.scheduler;

import com.logscanner.model.ErrorReport;
import com.logscanner.service.EmailService;
import com.logscanner.service.LogScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LogScanScheduler {

    private final LogScannerService logScannerService;
    private final EmailService emailService;

    @Value("${app.log.file-path}")
    private String logFilePath;

    // Runs every 5 minutes — adjust via app.scan.cron
    @Scheduled(cron = "${app.scan.cron:0 */5 * * * *}")
    public void scanAndAlert() {
        log.info("Starting log scan for: {}", logFilePath);
        try {
            ErrorReport report = logScannerService.scanLogFile(logFilePath);
            emailService.sendErrorAlert(report);
        } catch (Exception e) {
            log.error("Log scan failed", e);
        }
    }
}
