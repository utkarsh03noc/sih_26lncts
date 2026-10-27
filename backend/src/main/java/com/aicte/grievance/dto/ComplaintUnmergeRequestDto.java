package com.aicte.grievance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComplaintUnmergeRequestDto {

    @NotNull(message = "Primary complaint ID is required")
    private Long primaryComplaintId;

    @NotNull(message = "Secondary complaint ID to unmerge is required")
    private Long secondaryComplaintId;

    @NotBlank(message = "Unmerge justification is required for regulatory reversal")
    @Size(min = 10, max = 1000, message = "Unmerge reason must be between 10 and 1000 characters")
    private String unmergeReason;

    public ComplaintUnmergeRequestDto() {
    }

    public ComplaintUnmergeRequestDto(Long primaryComplaintId, Long secondaryComplaintId, String unmergeReason) {
        this.primaryComplaintId = primaryComplaintId;
        this.secondaryComplaintId = secondaryComplaintId;
        this.unmergeReason = unmergeReason;
    }

    public Long getPrimaryComplaintId() {
        return primaryComplaintId;
    }

    public void setPrimaryComplaintId(Long primaryComplaintId) {
        this.primaryComplaintId = primaryComplaintId;
    }

    public Long getSecondaryComplaintId() {
        return secondaryComplaintId;
    }

    public void setSecondaryComplaintId(Long secondaryComplaintId) {
        this.secondaryComplaintId = secondaryComplaintId;
    }

    public String getUnmergeReason() {
        return unmergeReason;
    }

    public void setUnmergeReason(String unmergeReason) {
        this.unmergeReason = unmergeReason;
    }
}
