package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComplaintCreateDto {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Institution is required")
    private Long institutionId;

    private Long bureauId;
    private Long cellId;
    private boolean duplicateAcknowledged = false;
    private String duplicateRemarks;

    public ComplaintCreateDto() {
    }

    // Getters and Setters
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public Long getBureauId() {
        return bureauId;
    }

    public void setBureauId(Long bureauId) {
        this.bureauId = bureauId;
    }

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
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
}
