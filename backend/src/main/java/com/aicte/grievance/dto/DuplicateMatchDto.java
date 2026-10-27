package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Complaint;
import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.ComplaintStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DuplicateMatchDto {
    private Long complaintId;
    private String complaintNumber;
    private String title;
    private ComplaintCategory category;
    private String categoryDisplayName;
    private ComplaintStatus status;
    private String institutionName;
    private String institutionCode;
    private String bureauName;
    private String cellName;
    private String assignedOfficerName;
    private LocalDateTime createdAt;
    private double similarityScore;
    private List<String> matchReasons = new ArrayList<>();
    private String matchType; // EXACT_MATCH, SEMANTIC_SIMILARITY, KEYWORD_OVERLAP, SEMANTIC_AND_KEYWORD
    private double semanticScore;
    private List<String> matchedConcepts = new ArrayList<>();
    private String matchExplanation;

    public DuplicateMatchDto() {
    }

    public static DuplicateMatchDto fromComplaint(Complaint c, double score, List<String> reasons) {
        return fromComplaint(c, score, reasons, "KEYWORD_OVERLAP", 0.0, Collections.emptyList(), null);
    }

    public static DuplicateMatchDto fromComplaint(Complaint c, double score, List<String> reasons,
                                                  String matchType, double semanticScore,
                                                  List<String> matchedConcepts, String matchExplanation) {
        if (c == null) return null;
        DuplicateMatchDto dto = new DuplicateMatchDto();
        dto.setComplaintId(c.getId());
        dto.setComplaintNumber(c.getComplaintNumber());
        dto.setTitle(c.getTitle());
        dto.setCategory(c.getCategory());
        dto.setCategoryDisplayName(c.getCategory() != null ? c.getCategory().getDisplayName() : null);
        dto.setStatus(c.getStatus());
        if (c.getInstitution() != null) {
            dto.setInstitutionName(c.getInstitution().getName());
            dto.setInstitutionCode(c.getInstitution().getCode());
        }
        if (c.getBureau() != null) {
            dto.setBureauName(c.getBureau().getName());
        }
        if (c.getCell() != null) {
            dto.setCellName(c.getCell().getName());
        }
        if (c.getAssignedOfficer() != null) {
            dto.setAssignedOfficerName(c.getAssignedOfficer().getFullName());
        }
        dto.setCreatedAt(c.getCreatedAt());
        dto.setSimilarityScore(Math.round(score * 100.0) / 100.0);
        dto.setMatchReasons(reasons != null ? reasons : new ArrayList<>());
        dto.setMatchType(matchType != null ? matchType : "KEYWORD_OVERLAP");
        dto.setSemanticScore(Math.round(semanticScore * 100.0) / 100.0);
        dto.setMatchedConcepts(matchedConcepts != null ? matchedConcepts : new ArrayList<>());
        dto.setMatchExplanation(matchExplanation);
        return dto;
    }

    // Getters and Setters
    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
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

    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getAssignedOfficerName() {
        return assignedOfficerName;
    }

    public void setAssignedOfficerName(String assignedOfficerName) {
        this.assignedOfficerName = assignedOfficerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public double getSemanticScore() {
        return semanticScore;
    }

    public void setSemanticScore(double semanticScore) {
        this.semanticScore = semanticScore;
    }

    public List<String> getMatchedConcepts() {
        return matchedConcepts;
    }

    public void setMatchedConcepts(List<String> matchedConcepts) {
        this.matchedConcepts = matchedConcepts;
    }

    public String getMatchExplanation() {
        return matchExplanation;
    }

    public void setMatchExplanation(String matchExplanation) {
        this.matchExplanation = matchExplanation;
    }
}
