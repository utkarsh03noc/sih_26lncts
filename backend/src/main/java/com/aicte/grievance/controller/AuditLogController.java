package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.AuditLogDto;
import com.aicte.grievance.dto.PagedResponse;
import com.aicte.grievance.service.AuditLogService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<AuditLogDto> logs = auditLogService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getEntityAuditLogs(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        List<AuditLogDto> logs = auditLogService.getEntityAuditLogs(entityName, entityId);
        return ResponseEntity.ok(ApiResponse.success("Entity audit history retrieved", logs));
    }
}
