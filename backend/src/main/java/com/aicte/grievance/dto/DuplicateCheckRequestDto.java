package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintCategory;
import jakarta.validation.constraints.NotNull;

public class DuplicateCheckRequestDto {

    @NotNull(message = "Institution ID is required")
    private Long institutionId;

    private ComplaintCategory category;
    private String title;
    private String description;
    private Double threshold; // optional threshold override (0-100)
    private Boolean crossInstitution; // optional, default false (institutions are primary boundary)

    public DuplicateCheckRequestDto() {
    }

    public DuplicateCheckRequestDto(String title, String description, Long institutionId, ComplaintCategory category) {
        this.title = title;
        this.description = description;
        this.institutionId = institutionId;
        this.category = category;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
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

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Boolean getCrossInstitution() {
        return crossInstitution;
    }

    public void setCrossInstitution(Boolean crossInstitution) {
        this.crossInstitution = crossInstitution;
    }
}
