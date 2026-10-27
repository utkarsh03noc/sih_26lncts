package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.AuditLogDto;
import com.aicte.grievance.dto.InstitutionDto;
import com.aicte.grievance.dto.InstitutionMonitoringDto;
import com.aicte.grievance.dto.PagedResponse;
import com.aicte.grievance.security.UserPrincipal;
import com.aicte.grievance.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InstitutionDto>>> getInstitutions(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<InstitutionDto> institutions = institutionService.getInstitutions(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Institutions retrieved", institutions));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<InstitutionDto>>> getActiveInstitutions() {
        List<InstitutionDto> list = institutionService.getActiveInstitutions();
        return ResponseEntity.ok(ApiResponse.success("Active institutions retrieved", list));
    }

    @GetMapping("/monitoring")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<PagedResponse<InstitutionMonitoringDto>>> getInstitutionMonitoring(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<InstitutionMonitoringDto> summary = institutionService.getInstitutionMonitoringSummary(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Institution monitoring summary retrieved", summary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstitutionDto>> getInstitutionById(@PathVariable Long id) {
        InstitutionDto dto = institutionService.getInstitutionById(id);
        return ResponseEntity.ok(ApiResponse.success("Institution retrieved", dto));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getInstitutionAuditLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<AuditLogDto> auditLogs = institutionService.getInstitutionAuditLogs(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Institution audit logs retrieved", auditLogs));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstitutionDto>> createInstitution(
            @Valid @RequestBody InstitutionDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        InstitutionDto created = institutionService.createInstitution(dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Institution created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstitutionDto>> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        InstitutionDto updated = institutionService.updateInstitution(id, dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Institution updated successfully", updated));
    }
}
