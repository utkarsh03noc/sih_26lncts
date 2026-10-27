package com.aicte.grievance.dto;

public class InstitutionComplaintStatDto {
    private Long institutionId;
    private String institutionName;
    private long totalComplaints;
    private long resolvedComplaints;
    private long escalatedComplaints;
    private double resolutionRate;

    public InstitutionComplaintStatDto() {
    }

    public InstitutionComplaintStatDto(Long institutionId, String institutionName, long totalComplaints, long resolvedComplaints, long escalatedComplaints, double resolutionRate) {
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.totalComplaints = totalComplaints;
        this.resolvedComplaints = resolvedComplaints;
        this.escalatedComplaints = escalatedComplaints;
        this.resolutionRate = resolutionRate;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public long getEscalatedComplaints() {
        return escalatedComplaints;
    }

    public void setEscalatedComplaints(long escalatedComplaints) {
        this.escalatedComplaints = escalatedComplaints;
    }

    public double getResolutionRate() {
        return resolutionRate;
    }

    public void setResolutionRate(double resolutionRate) {
        this.resolutionRate = resolutionRate;
    }
}
