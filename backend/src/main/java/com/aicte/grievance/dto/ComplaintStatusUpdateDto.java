package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ComplaintStatusUpdateDto {

    @NotNull(message = "New status is required")
    private ComplaintStatus newStatus;

    @NotBlank(message = "Remarks are required for status update")
    private String remarks;

    public ComplaintStatusUpdateDto() {
    }

    public ComplaintStatusUpdateDto(ComplaintStatus newStatus, String remarks) {
        this.newStatus = newStatus;
        this.remarks = remarks;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ComplaintStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
