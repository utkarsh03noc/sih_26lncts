package com.aicte.grievance.dto;

import jakarta.validation.constraints.NotNull;

public class ComplaintAssignDto {

    private Long institutionId;
    private Long bureauId;
    private Long cellId;

    @NotNull(message = "Officer ID is required")
    private Long officerId;

    private String remarks;

    public ComplaintAssignDto() {
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

    public Long getOfficerId() {
        return officerId;
    }

    public void setOfficerId(Long officerId) {
        this.officerId = officerId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
