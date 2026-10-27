package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.DashboardStatsDto;
import com.aicte.grievance.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final DashboardService dashboardService;

    public ReportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummaryReport() {
        DashboardStatsDto stats = dashboardService.getOverallDashboardStats();

        Map<String, Object> report = new HashMap<>();
        report.put("generatedDate", LocalDate.now());
        report.put("portalTitle", "Integrated Dashboard for Monitoring and Tracking Institution-Related Complaint Cases Across AICTE");
        report.put("stats", stats);
        report.put("systemHealth", "Operational");

        return ResponseEntity.ok(ApiResponse.success("Executive summary report generated", report));
    }
}
