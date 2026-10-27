package com.aicte.grievance.dto;

public class BureauStatDto {
    private Long bureauId;
    private String bureauName;
    private String bureauCode;
    private long totalCount;
    private long resolvedCount;
    private long pendingCount;

    public BureauStatDto() {
    }

    public BureauStatDto(Long bureauId, String bureauName, String bureauCode, long totalCount, long resolvedCount, long pendingCount) {
        this.bureauId = bureauId;
        this.bureauName = bureauName;
        this.bureauCode = bureauCode;
        this.totalCount = totalCount;
        this.resolvedCount = resolvedCount;
        this.pendingCount = pendingCount;
    }

    public Long getBureauId() {
        return bureauId;
    }

    public void setBureauId(Long bureauId) {
        this.bureauId = bureauId;
    }

    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }

    public String getBureauCode() {
        return bureauCode;
    }

    public void setBureauCode(String bureauCode) {
        this.bureauCode = bureauCode;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(long resolvedCount) {
        this.resolvedCount = resolvedCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }
}
