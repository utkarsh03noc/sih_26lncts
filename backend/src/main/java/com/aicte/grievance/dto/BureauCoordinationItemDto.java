package com.aicte.grievance.dto;

public class BureauCoordinationItemDto {
    private String bureauCode;
    private String bureauName;
    private String cellCode;
    private String cellName;
    private String complaintNumber;
    private Long complaintId;
    private String assignedOfficerName;
    private String roleInCase;

    public BureauCoordinationItemDto() {
    }

    public BureauCoordinationItemDto(String bureauCode, String bureauName, String cellCode, String cellName,
                                     String complaintNumber, Long complaintId, String assignedOfficerName, String roleInCase) {
        this.bureauCode = bureauCode;
        this.bureauName = bureauName;
        this.cellCode = cellCode;
        this.cellName = cellName;
        this.complaintNumber = complaintNumber;
        this.complaintId = complaintId;
        this.assignedOfficerName = assignedOfficerName;
        this.roleInCase = roleInCase;
    }

    public String getBureauCode() {
        return bureauCode;
    }

    public void setBureauCode(String bureauCode) {
        this.bureauCode = bureauCode;
    }

    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }

    public String getCellCode() {
        return cellCode;
    }

    public void setCellCode(String cellCode) {
        this.cellCode = cellCode;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getAssignedOfficerName() {
        return assignedOfficerName;
    }

    public void setAssignedOfficerName(String assignedOfficerName) {
        this.assignedOfficerName = assignedOfficerName;
    }

    public String getRoleInCase() {
        return roleInCase;
    }

    public void setRoleInCase(String roleInCase) {
        this.roleInCase = roleInCase;
    }
}
