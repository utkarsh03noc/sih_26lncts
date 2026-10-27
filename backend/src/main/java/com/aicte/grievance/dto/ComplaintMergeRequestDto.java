package com.aicte.grievance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComplaintMergeRequestDto {

    @NotNull(message = "Primary master complaint ID is required")
    private Long primaryComplaintId;

    @NotNull(message = "Secondary complaint ID to merge is required")
    private Long secondaryComplaintId;

    @NotBlank(message = "Merge reason / justification is mandatory for officer approval")
    @Size(min = 10, max = 1000, message = "Merge reason must be between 10 and 1000 characters")
    private String mergeReason;

    private Double similarityScore;

    private boolean crossInstitutionAuthorized = false;

    public ComplaintMergeRequestDto() {
    }

    public ComplaintMergeRequestDto(Long primaryComplaintId, Long secondaryComplaintId, String mergeReason) {
        this.primaryComplaintId = primaryComplaintId;
        this.secondaryComplaintId = secondaryComplaintId;
        this.mergeReason = mergeReason;
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

    public String getMergeReason() {
        return mergeReason;
    }

    public void setMergeReason(String mergeReason) {
        this.mergeReason = mergeReason;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public boolean isCrossInstitutionAuthorized() {
        return crossInstitutionAuthorized;
    }

    public void setCrossInstitutionAuthorized(boolean crossInstitutionAuthorized) {
        this.crossInstitutionAuthorized = crossInstitutionAuthorized;
    }
}
