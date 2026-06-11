package com.logscanner.controller;


import com.logscanner.model.ScanResult;
import com.logscanner.service.ScanResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {


    private final ScanResultService scanResultService;

    // GET dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(scanResultService.getDashboardStats());
    }

    // GET all errors
    @GetMapping("/errors")
    public ResponseEntity<List<ScanResult>> getAllErrors() {
        return ResponseEntity.ok(scanResultService.getAllErrors());
    }

    // GET errors by severity
    @GetMapping("/errors/{severity}")
    public ResponseEntity<List<ScanResult>> getErrorsBySeverity(
            @PathVariable String severity) {
        return ResponseEntity.ok(
                scanResultService.getErrorsBySeverity(severity));
    }

    // GET recent errors (last 24 hours)
    @GetMapping("/errors/recent")
    public ResponseEntity<List<ScanResult>> getRecentErrors() {
        return ResponseEntity.ok(scanResultService.getRecentErrors());
    }
}
