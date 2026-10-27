package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Complaint;
import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.Priority;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ComplaintResponseDto {
    private Long id;
    private String complaintNumber;
    private String title;
    private String description;
    private ComplaintCategory category;
    private String categoryDisplayName;
    private Priority priority;
    private ComplaintStatus status;
    private Long complainantId;
    private String complainantName;
    private String complainantEmail;
    private String complainantPhone;
    private Long institutionId;
    private String institutionName;
    private String institutionCode;
    private Long assignedOfficerId;
    private String assignedOfficerName;
    private Long bureauId;
    private String bureauName;
    private String bureauCode;
    private Long cellId;
    private String cellName;
    private String cellCode;
    private LocalDateTime assignmentDate;
    private String assignmentRemarks;
    private boolean potentialDuplicate;
    private boolean duplicateAcknowledged;
    private String duplicateRemarks;
    private int transfersCount;
    private LocalDate dueDate;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean overdue;
    private boolean merged;
    private Long mergedIntoComplaintId;
    private String mergedIntoComplaintNumber;
    private int mergedCasesCount;
    private int relatedCasesCount;

    public ComplaintResponseDto() {
    }

    public static ComplaintResponseDto fromEntity(Complaint c) {
        if (c == null) return null;
        ComplaintResponseDto dto = new ComplaintResponseDto();
        dto.setId(c.getId());
        dto.setComplaintNumber(c.getComplaintNumber());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setCategory(c.getCategory());
        dto.setCategoryDisplayName(c.getCategory() != null ? c.getCategory().getDisplayName() : null);
        dto.setPriority(c.getPriority());
        dto.setStatus(c.getStatus());

        if (c.getComplainant() != null) {
            dto.setComplainantId(c.getComplainant().getId());
            dto.setComplainantName(c.getComplainant().getFullName());
            dto.setComplainantEmail(c.getComplainant().getEmail());
            dto.setComplainantPhone(c.getComplainant().getPhone());
        }

        if (c.getInstitution() != null) {
            dto.setInstitutionId(c.getInstitution().getId());
            dto.setInstitutionName(c.getInstitution().getName());
            dto.setInstitutionCode(c.getInstitution().getCode());
        }

        if (c.getAssignedOfficer() != null) {
            dto.setAssignedOfficerId(c.getAssignedOfficer().getId());
            dto.setAssignedOfficerName(c.getAssignedOfficer().getFullName());
        }

        if (c.getBureau() != null) {
            dto.setBureauId(c.getBureau().getId());
            dto.setBureauName(c.getBureau().getName());
            dto.setBureauCode(c.getBureau().getCode());
        }

        if (c.getCell() != null) {
            dto.setCellId(c.getCell().getId());
            dto.setCellName(c.getCell().getName());
            dto.setCellCode(c.getCell().getCode());
        }

        dto.setAssignmentDate(c.getAssignmentDate());
        dto.setAssignmentRemarks(c.getAssignmentRemarks());
        dto.setPotentialDuplicate(c.isPotentialDuplicate());
        dto.setDuplicateAcknowledged(c.isDuplicateAcknowledged());
        dto.setDuplicateRemarks(c.getDuplicateRemarks());

        dto.setDueDate(c.getDueDate());
        dto.setResolvedAt(c.getResolvedAt());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        boolean isResolvedOrClosed = c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED;
        dto.setOverdue(!isResolvedOrClosed && c.getDueDate() != null && c.getDueDate().isBefore(LocalDate.now()));

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public Long getComplainantId() {
        return complainantId;
    }

    public void setComplainantId(Long complainantId) {
        this.complainantId = complainantId;
    }

    public String getComplainantName() {
        return complainantName;
    }

    public void setComplainantName(String complainantName) {
        this.complainantName = complainantName;
    }

    public String getComplainantEmail() {
        return complainantEmail;
    }

    public void setComplainantEmail(String complainantEmail) {
        this.complainantEmail = complainantEmail;
    }

    public String getComplainantPhone() {
        return complainantPhone;
    }

    public void setComplainantPhone(String complainantPhone) {
        this.complainantPhone = complainantPhone;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public Long getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(Long assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public String getAssignedOfficerName() {
        return assignedOfficerName;
    }

    public void setAssignedOfficerName(String assignedOfficerName) {
        this.assignedOfficerName = assignedOfficerName;
    }

    public Long getBureauId() {
        return bureauId;
    }

    public void setBureauId(Long bureauId) {
        this.bureauId = bureauId;
    }

    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }

    public String getBureauCode() {
        return bureauCode;
    }

    public void setBureauCode(String bureauCode) {
        this.bureauCode = bureauCode;
    }

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getCellCode() {
        return cellCode;
    }

    public void setCellCode(String cellCode) {
        this.cellCode = cellCode;
    }

    public LocalDateTime getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDateTime assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getAssignmentRemarks() {
        return assignmentRemarks;
    }

    public void setAssignmentRemarks(String assignmentRemarks) {
        this.assignmentRemarks = assignmentRemarks;
    }

    public boolean isPotentialDuplicate() {
        return potentialDuplicate;
    }

    public void setPotentialDuplicate(boolean potentialDuplicate) {
        this.potentialDuplicate = potentialDuplicate;
    }

    public boolean isDuplicateAcknowledged() {
        return duplicateAcknowledged;
    }

    public void setDuplicateAcknowledged(boolean duplicateAcknowledged) {
        this.duplicateAcknowledged = duplicateAcknowledged;
    }

    public String getDuplicateRemarks() {
        return duplicateRemarks;
    }

    public void setDuplicateRemarks(String duplicateRemarks) {
        this.duplicateRemarks = duplicateRemarks;
    }

    public int getTransfersCount() {
        return transfersCount;
    }

    public void setTransfersCount(int transfersCount) {
        this.transfersCount = transfersCount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public Long getMergedIntoComplaintId() {
        return mergedIntoComplaintId;
    }

    public void setMergedIntoComplaintId(Long mergedIntoComplaintId) {
        this.mergedIntoComplaintId = mergedIntoComplaintId;
    }

    public String getMergedIntoComplaintNumber() {
        return mergedIntoComplaintNumber;
    }

    public void setMergedIntoComplaintNumber(String mergedIntoComplaintNumber) {
        this.mergedIntoComplaintNumber = mergedIntoComplaintNumber;
    }

    public int getMergedCasesCount() {
        return mergedCasesCount;
    }

    public void setMergedCasesCount(int mergedCasesCount) {
        this.mergedCasesCount = mergedCasesCount;
    }

    public int getRelatedCasesCount() {
        return relatedCasesCount;
    }

    public void setRelatedCasesCount(int relatedCasesCount) {
        this.relatedCasesCount = relatedCasesCount;
    }
}
