package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.ComplaintUpdate;
import java.time.LocalDateTime;

public class ComplaintUpdateDto {
    private Long id;
    private Long complaintId;
    private ComplaintStatus previousStatus;
    private ComplaintStatus newStatus;
    private String remarks;
    private Long updatedById;
    private String updatedByName;
    private String updatedByRole;
    private LocalDateTime createdAt;

    public ComplaintUpdateDto() {
    }

    public static ComplaintUpdateDto fromEntity(ComplaintUpdate u) {
        if (u == null) return null;
        ComplaintUpdateDto dto = new ComplaintUpdateDto();
        dto.setId(u.getId());
        if (u.getComplaint() != null) {
            dto.setComplaintId(u.getComplaint().getId());
        }
        dto.setPreviousStatus(u.getPreviousStatus());
        dto.setNewStatus(u.getNewStatus());
        dto.setRemarks(u.getRemarks());
        if (u.getUpdatedBy() != null) {
            dto.setUpdatedById(u.getUpdatedBy().getId());
            dto.setUpdatedByName(u.getUpdatedBy().getFullName());
            dto.setUpdatedByRole(u.getUpdatedBy().getRole().name());
        }
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ComplaintStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
