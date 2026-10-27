package com.aicte.grievance.service;

import com.aicte.grievance.dto.AuditLogDto;
import com.aicte.grievance.dto.PagedResponse;
import com.aicte.grievance.entity.AuditLog;
import com.aicte.grievance.entity.User;
import com.aicte.grievance.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String action, User performedBy, String entityName, Long entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog(action, performedBy, entityName, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        List<AuditLogDto> dtos = page.getContent().stream()
                .map(AuditLogDto::fromEntity)
                .toList();
        return PagedResponse.of(page, dtos);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getEntityAuditLogs(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId).stream()
                .map(AuditLogDto::fromEntity)
                .toList();
    }
}
