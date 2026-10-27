package com.aicte.grievance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComplaintClassificationRequestDto {

    @NotBlank(message = "Title is required for classification")
    @Size(max = 300, message = "Title cannot exceed 300 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    private Long institutionId;

    public ComplaintClassificationRequestDto() {
    }

    public ComplaintClassificationRequestDto(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public ComplaintClassificationRequestDto(String title, String description, Long institutionId) {
        this.title = title;
        this.description = description;
        this.institutionId = institutionId;
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

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }
}
