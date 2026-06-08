package com.logscanner.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorReport {

    private String logFilePath;
    private LocalDateTime scannedAt;
    private int totalErrors;
    private List<ErrorEntry> errors;

    @Data
    @Builder
    public static class ErrorEntry {
        private int lineNumber;
        private String errorType;       // e.g., NullPointerException
        private String errorMessage;
        private String rootCause;       // traced root cause
        private List<String> stackTrace;
        private String severity;        // ERROR, FATAL, WARN
    }
}
