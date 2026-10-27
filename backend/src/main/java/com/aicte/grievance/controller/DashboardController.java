package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.DashboardStatsDto;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.Priority;
import com.aicte.grievance.security.UserPrincipal;
import com.aicte.grievance.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getOverallStats(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long bureauId,
            @RequestParam(required = false) Long cellId,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Priority priority) {
        DashboardStatsDto stats = dashboardService.getFilteredDashboardStats(institutionId, bureauId, cellId, status, priority);
        return ResponseEntity.ok(ApiResponse.success("Overall dashboard statistics retrieved", stats));
    }

    @GetMapping("/institution-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getInstitutionStats(
            @RequestParam(required = false) Long institutionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long targetInstId = institutionId;
        boolean isOfficer = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_OFFICER"));

        if (isOfficer) {
            targetInstId = currentUser.getInstitutionId();
        }

        if (targetInstId == null) {
            DashboardStatsDto stats = dashboardService.getOverallDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved", stats));
        }

        DashboardStatsDto stats = dashboardService.getInstitutionDashboardStats(targetInstId);
        return ResponseEntity.ok(ApiResponse.success("Institution dashboard statistics retrieved", stats));
    }

    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('COMPLAINANT')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getComplainantStats(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardStatsDto stats = dashboardService.getComplainantDashboardStats(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Complainant statistics retrieved", stats));
    }
}
