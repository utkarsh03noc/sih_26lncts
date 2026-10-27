package com.aicte.grievance.dto;

import com.aicte.grievance.entity.AuditLog;
import java.time.LocalDateTime;

public class AuditLogDto {
    private Long id;
    private String action;
    private Long performedById;
    private String performedByName;
    private String performedByRole;
    private String entityName;
    private Long entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;

    public AuditLogDto() {
    }

    public static AuditLogDto fromEntity(AuditLog log) {
        if (log == null) return null;
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setAction(log.getAction());
        if (log.getPerformedBy() != null) {
            dto.setPerformedById(log.getPerformedBy().getId());
            dto.setPerformedByName(log.getPerformedBy().getFullName());
            dto.setPerformedByRole(log.getPerformedBy().getRole().name());
        }
        dto.setEntityName(log.getEntityName());
        dto.setEntityId(log.getEntityId());
        dto.setDetails(log.getDetails());
        dto.setIpAddress(log.getIpAddress());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getPerformedById() {
        return performedById;
    }

    public void setPerformedById(Long performedById) {
        this.performedById = performedById;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
