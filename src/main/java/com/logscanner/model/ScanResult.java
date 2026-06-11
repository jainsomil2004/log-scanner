package com.logscanner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scan_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logFilePath;
    private LocalDateTime scannedAt;
    private int totalErrors;

    private String severity;        // ERROR, FATAL
    private String errorType;       // NullPointerException etc
    private int lineNumber;

    @Column(length = 2000)
    private String errorMessage;

    @Column(length = 2000)
    private String rootCause;

    @Column(length = 5000)
    private String stackTrace;

    private boolean emailSent;
    private LocalDateTime emailSentAt;
}
