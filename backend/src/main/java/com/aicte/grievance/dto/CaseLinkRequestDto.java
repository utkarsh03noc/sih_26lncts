package com.aicte.grievance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CaseLinkRequestDto {

    @NotNull(message = "Source complaint ID is required")
    private Long sourceComplaintId;

    @NotNull(message = "Target complaint ID is required")
    private Long targetComplaintId;

    @NotBlank(message = "Link reason / contextual relation note is required")
    @Size(min = 5, max = 1000, message = "Link reason must be between 5 and 1000 characters")
    private String linkReason;

    public CaseLinkRequestDto() {
    }

    public CaseLinkRequestDto(Long sourceComplaintId, Long targetComplaintId, String linkReason) {
        this.sourceComplaintId = sourceComplaintId;
        this.targetComplaintId = targetComplaintId;
        this.linkReason = linkReason;
    }

    public Long getSourceComplaintId() {
        return sourceComplaintId;
    }

    public void setSourceComplaintId(Long sourceComplaintId) {
        this.sourceComplaintId = sourceComplaintId;
    }

    public Long getTargetComplaintId() {
        return targetComplaintId;
    }

    public void setTargetComplaintId(Long targetComplaintId) {
        this.targetComplaintId = targetComplaintId;
    }

    public String getLinkReason() {
        return linkReason;
    }

    public void setLinkReason(String linkReason) {
        this.linkReason = linkReason;
    }
}
