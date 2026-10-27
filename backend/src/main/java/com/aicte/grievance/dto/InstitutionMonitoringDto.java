package com.aicte.grievance.dto;

import java.util.ArrayList;
import java.util.List;

public class InstitutionMonitoringDto {
    private Long institutionId;
    private String institutionName;
    private String institutionCode;
    private String state;
    private String district;
    private String type;
    private long totalComplaints;
    private long openComplaints;
    private long resolvedComplaints;
    private long overdueComplaints;
    private int involvedBureausCount;
    private int involvedCellsCount;
    private List<String> involvedBureauNames = new ArrayList<>();
    private List<String> involvedCellNames = new ArrayList<>();

    public InstitutionMonitoringDto() {
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

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getOpenComplaints() {
        return openComplaints;
    }

    public void setOpenComplaints(long openComplaints) {
        this.openComplaints = openComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public long getOverdueComplaints() {
        return overdueComplaints;
    }

    public void setOverdueComplaints(long overdueComplaints) {
        this.overdueComplaints = overdueComplaints;
    }

    public int getInvolvedBureausCount() {
        return involvedBureausCount;
    }

    public void setInvolvedBureausCount(int involvedBureausCount) {
        this.involvedBureausCount = involvedBureausCount;
    }

    public int getInvolvedCellsCount() {
        return involvedCellsCount;
    }

    public void setInvolvedCellsCount(int involvedCellsCount) {
        this.involvedCellsCount = involvedCellsCount;
    }

    public List<String> getInvolvedBureauNames() {
        return involvedBureauNames;
    }

    public void setInvolvedBureauNames(List<String> involvedBureauNames) {
        this.involvedBureauNames = involvedBureauNames;
    }

    public List<String> getInvolvedCellNames() {
        return involvedCellNames;
    }

    public void setInvolvedCellNames(List<String> involvedCellNames) {
        this.involvedCellNames = involvedCellNames;
    }
}
