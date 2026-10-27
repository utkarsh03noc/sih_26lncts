package com.aicte.grievance.dto;

import java.util.ArrayList;
import java.util.List;

public class UnifiedCaseDto {
    private String unifiedCaseNumber;
    private Long primaryComplaintId;
    private ComplaintResponseDto primaryComplaint;
    private List<ComplaintResponseDto> mergedComplaints = new ArrayList<>();
    private List<ComplaintResponseDto> relatedComplaints = new ArrayList<>();
    private List<ConsolidatedTimelineItemDto> consolidatedTimeline = new ArrayList<>();
    private List<BureauCoordinationItemDto> crossBureauCoordination = new ArrayList<>();
    private List<AttachmentDto> consolidatedAttachments = new ArrayList<>();
    private List<CaseRelationDto> mergeHistory = new ArrayList<>();
    private int totalMergedCases;
    private int totalRelatedCases;

    public UnifiedCaseDto() {
    }

    public String getUnifiedCaseNumber() {
        return unifiedCaseNumber;
    }

    public void setUnifiedCaseNumber(String unifiedCaseNumber) {
        this.unifiedCaseNumber = unifiedCaseNumber;
    }

    public Long getPrimaryComplaintId() {
        return primaryComplaintId;
    }

    public void setPrimaryComplaintId(Long primaryComplaintId) {
        this.primaryComplaintId = primaryComplaintId;
    }

    public ComplaintResponseDto getPrimaryComplaint() {
        return primaryComplaint;
    }

    public void setPrimaryComplaint(ComplaintResponseDto primaryComplaint) {
        this.primaryComplaint = primaryComplaint;
    }

    public List<ComplaintResponseDto> getMergedComplaints() {
        return mergedComplaints;
    }

    public void setMergedComplaints(List<ComplaintResponseDto> mergedComplaints) {
        this.mergedComplaints = mergedComplaints;
    }

    public List<ComplaintResponseDto> getRelatedComplaints() {
        return relatedComplaints;
    }

    public void setRelatedComplaints(List<ComplaintResponseDto> relatedComplaints) {
        this.relatedComplaints = relatedComplaints;
    }

    public List<ConsolidatedTimelineItemDto> getConsolidatedTimeline() {
        return consolidatedTimeline;
    }

    public void setConsolidatedTimeline(List<ConsolidatedTimelineItemDto> consolidatedTimeline) {
        this.consolidatedTimeline = consolidatedTimeline;
    }

    public List<BureauCoordinationItemDto> getCrossBureauCoordination() {
        return crossBureauCoordination;
    }

    public void setCrossBureauCoordination(List<BureauCoordinationItemDto> crossBureauCoordination) {
        this.crossBureauCoordination = crossBureauCoordination;
    }

    public List<AttachmentDto> getConsolidatedAttachments() {
        return consolidatedAttachments;
    }

    public void setConsolidatedAttachments(List<AttachmentDto> consolidatedAttachments) {
        this.consolidatedAttachments = consolidatedAttachments;
    }

    public List<CaseRelationDto> getMergeHistory() {
        return mergeHistory;
    }

    public void setMergeHistory(List<CaseRelationDto> mergeHistory) {
        this.mergeHistory = mergeHistory;
    }

    public int getTotalMergedCases() {
        return totalMergedCases;
    }

    public void setTotalMergedCases(int totalMergedCases) {
        this.totalMergedCases = totalMergedCases;
    }

    public int getTotalRelatedCases() {
        return totalRelatedCases;
    }

    public void setTotalRelatedCases(int totalRelatedCases) {
        this.totalRelatedCases = totalRelatedCases;
    }
}
