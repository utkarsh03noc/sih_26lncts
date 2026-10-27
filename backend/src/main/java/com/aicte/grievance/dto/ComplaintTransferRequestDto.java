package com.aicte.grievance.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintTransferRequestDto {
    private Long targetBureauId;
    private Long targetCellId;
    private Long targetOfficerId;

    @NotBlank(message = "Transfer reason is mandatory")
    private String transferReason;

    private String remarks;

    public ComplaintTransferRequestDto() {
    }

    public Long getTargetBureauId() {
        return targetBureauId;
    }

    public void setTargetBureauId(Long targetBureauId) {
        this.targetBureauId = targetBureauId;
    }

    public Long getTargetCellId() {
        return targetCellId;
    }

    public void setTargetCellId(Long targetCellId) {
        this.targetCellId = targetCellId;
    }

    public Long getTargetOfficerId() {
        return targetOfficerId;
    }

    public void setTargetOfficerId(Long targetOfficerId) {
        this.targetOfficerId = targetOfficerId;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
