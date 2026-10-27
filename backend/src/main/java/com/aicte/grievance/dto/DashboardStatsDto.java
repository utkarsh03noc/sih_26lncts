package com.aicte.grievance.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardStatsDto {
    private long totalInstitutions;
    private long totalComplaints;
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long closedComplaints;
    private long escalatedComplaints;
    private long overdueComplaints;
    private long potentialDuplicateCount;
    private double resolutionRate;
    private double averageResolutionDays;

    private List<CategoryStatDto> categoryStats = new ArrayList<>();
    private List<PriorityStatDto> priorityStats = new ArrayList<>();
    private List<StatusStatDto> statusStats = new ArrayList<>();
    private List<MonthlyTrendDto> monthlyTrend = new ArrayList<>();
    private List<InstitutionComplaintStatDto> institutionStats = new ArrayList<>();
    private List<BureauStatDto> bureauStats = new ArrayList<>();
    private List<CellStatDto> cellStats = new ArrayList<>();

    public DashboardStatsDto() {
    }

    public long getTotalInstitutions() {
        return totalInstitutions;
    }

    public void setTotalInstitutions(long totalInstitutions) {
        this.totalInstitutions = totalInstitutions;
    }

    public long getPotentialDuplicateCount() {
        return potentialDuplicateCount;
    }

    public void setPotentialDuplicateCount(long potentialDuplicateCount) {
        this.potentialDuplicateCount = potentialDuplicateCount;
    }

    public List<BureauStatDto> getBureauStats() {
        return bureauStats;
    }

    public void setBureauStats(List<BureauStatDto> bureauStats) {
        this.bureauStats = bureauStats;
    }

    public List<CellStatDto> getCellStats() {
        return cellStats;
    }

    public void setCellStats(List<CellStatDto> cellStats) {
        this.cellStats = cellStats;
    }

    // Getters and Setters
    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getPendingComplaints() {
        return pendingComplaints;
    }

    public void setPendingComplaints(long pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }

    public long getInProgressComplaints() {
        return inProgressComplaints;
    }

    public void setInProgressComplaints(long inProgressComplaints) {
        this.inProgressComplaints = inProgressComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public long getClosedComplaints() {
        return closedComplaints;
    }

    public void setClosedComplaints(long closedComplaints) {
        this.closedComplaints = closedComplaints;
    }

    public long getEscalatedComplaints() {
        return escalatedComplaints;
    }

    public void setEscalatedComplaints(long escalatedComplaints) {
        this.escalatedComplaints = escalatedComplaints;
    }

    public long getOverdueComplaints() {
        return overdueComplaints;
    }

    public void setOverdueComplaints(long overdueComplaints) {
        this.overdueComplaints = overdueComplaints;
    }

    public double getResolutionRate() {
        return resolutionRate;
    }

    public void setResolutionRate(double resolutionRate) {
        this.resolutionRate = resolutionRate;
    }

    public double getAverageResolutionDays() {
        return averageResolutionDays;
    }

    public void setAverageResolutionDays(double averageResolutionDays) {
        this.averageResolutionDays = averageResolutionDays;
    }

    public List<CategoryStatDto> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(List<CategoryStatDto> categoryStats) {
        this.categoryStats = categoryStats;
    }

    public List<PriorityStatDto> getPriorityStats() {
        return priorityStats;
    }

    public void setPriorityStats(List<PriorityStatDto> priorityStats) {
        this.priorityStats = priorityStats;
    }

    public List<StatusStatDto> getStatusStats() {
        return statusStats;
    }

    public void setStatusStats(List<StatusStatDto> statusStats) {
        this.statusStats = statusStats;
    }

    public List<MonthlyTrendDto> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<MonthlyTrendDto> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public List<InstitutionComplaintStatDto> getInstitutionStats() {
        return institutionStats;
    }

    public void setInstitutionStats(List<InstitutionComplaintStatDto> institutionStats) {
        this.institutionStats = institutionStats;
    }
}
