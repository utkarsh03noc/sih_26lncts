package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintTransfer;
import java.time.LocalDateTime;

public class ComplaintTransferDto {
    private Long id;
    private Long complaintId;
    private String complaintNumber;

    private Long fromBureauId;
    private String fromBureauName;
    private String fromBureauCode;

    private Long toBureauId;
    private String toBureauName;
    private String toBureauCode;

    private Long fromCellId;
    private String fromCellName;
    private String fromCellCode;

    private Long toCellId;
    private String toCellName;
    private String toCellCode;

    private Long fromOfficerId;
    private String fromOfficerName;

    private Long toOfficerId;
    private String toOfficerName;

    private String transferReason;
    private Long transferredById;
    private String transferredByName;
    private LocalDateTime transferredAt;

    public ComplaintTransferDto() {
    }

    public static ComplaintTransferDto fromEntity(ComplaintTransfer ct) {
        if (ct == null) return null;
        ComplaintTransferDto dto = new ComplaintTransferDto();
        dto.setId(ct.getId());
        if (ct.getComplaint() != null) {
            dto.setComplaintId(ct.getComplaint().getId());
            dto.setComplaintNumber(ct.getComplaint().getComplaintNumber());
        }
        if (ct.getFromBureau() != null) {
            dto.setFromBureauId(ct.getFromBureau().getId());
            dto.setFromBureauName(ct.getFromBureau().getName());
            dto.setFromBureauCode(ct.getFromBureau().getCode());
        }
        if (ct.getToBureau() != null) {
            dto.setToBureauId(ct.getToBureau().getId());
            dto.setToBureauName(ct.getToBureau().getName());
            dto.setToBureauCode(ct.getToBureau().getCode());
        }
        if (ct.getFromCell() != null) {
            dto.setFromCellId(ct.getFromCell().getId());
            dto.setFromCellName(ct.getFromCell().getName());
            dto.setFromCellCode(ct.getFromCell().getCode());
        }
        if (ct.getToCell() != null) {
            dto.setToCellId(ct.getToCell().getId());
            dto.setToCellName(ct.getToCell().getName());
            dto.setToCellCode(ct.getToCell().getCode());
        }
        if (ct.getFromOfficer() != null) {
            dto.setFromOfficerId(ct.getFromOfficer().getId());
            dto.setFromOfficerName(ct.getFromOfficer().getFullName());
        }
        if (ct.getToOfficer() != null) {
            dto.setToOfficerId(ct.getToOfficer().getId());
            dto.setToOfficerName(ct.getToOfficer().getFullName());
        }
        dto.setTransferReason(ct.getTransferReason());
        if (ct.getTransferredBy() != null) {
            dto.setTransferredById(ct.getTransferredBy().getId());
            dto.setTransferredByName(ct.getTransferredBy().getFullName());
        }
        dto.setTransferredAt(ct.getTransferredAt());
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

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public Long getFromBureauId() {
        return fromBureauId;
    }

    public void setFromBureauId(Long fromBureauId) {
        this.fromBureauId = fromBureauId;
    }

    public String getFromBureauName() {
        return fromBureauName;
    }

    public void setFromBureauName(String fromBureauName) {
        this.fromBureauName = fromBureauName;
    }

    public String getFromBureauCode() {
        return fromBureauCode;
    }

    public void setFromBureauCode(String fromBureauCode) {
        this.fromBureauCode = fromBureauCode;
    }

    public Long getToBureauId() {
        return toBureauId;
    }

    public void setToBureauId(Long toBureauId) {
        this.toBureauId = toBureauId;
    }

    public String getToBureauName() {
        return toBureauName;
    }

    public void setToBureauName(String toBureauName) {
        this.toBureauName = toBureauName;
    }

    public String getToBureauCode() {
        return toBureauCode;
    }

    public void setToBureauCode(String toBureauCode) {
        this.toBureauCode = toBureauCode;
    }

    public Long getFromCellId() {
        return fromCellId;
    }

    public void setFromCellId(Long fromCellId) {
        this.fromCellId = fromCellId;
    }

    public String getFromCellName() {
        return fromCellName;
    }

    public void setFromCellName(String fromCellName) {
        this.fromCellName = fromCellName;
    }

    public String getFromCellCode() {
        return fromCellCode;
    }

    public void setFromCellCode(String fromCellCode) {
        this.fromCellCode = fromCellCode;
    }

    public Long getToCellId() {
        return toCellId;
    }

    public void setToCellId(Long toCellId) {
        this.toCellId = toCellId;
    }

    public String getToCellName() {
        return toCellName;
    }

    public void setToCellName(String toCellName) {
        this.toCellName = toCellName;
    }

    public String getToCellCode() {
        return toCellCode;
    }

    public void setToCellCode(String toCellCode) {
        this.toCellCode = toCellCode;
    }

    public Long getFromOfficerId() {
        return fromOfficerId;
    }

    public void setFromOfficerId(Long fromOfficerId) {
        this.fromOfficerId = fromOfficerId;
    }

    public String getFromOfficerName() {
        return fromOfficerName;
    }

    public void setFromOfficerName(String fromOfficerName) {
        this.fromOfficerName = fromOfficerName;
    }

    public Long getToOfficerId() {
        return toOfficerId;
    }

    public void setToOfficerId(Long toOfficerId) {
        this.toOfficerId = toOfficerId;
    }

    public String getToOfficerName() {
        return toOfficerName;
    }

    public void setToOfficerName(String toOfficerName) {
        this.toOfficerName = toOfficerName;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public Long getTransferredById() {
        return transferredById;
    }

    public void setTransferredById(Long transferredById) {
        this.transferredById = transferredById;
    }

    public String getTransferredByName() {
        return transferredByName;
    }

    public void setTransferredByName(String transferredByName) {
        this.transferredByName = transferredByName;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }
}
