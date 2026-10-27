package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintRelation;
import java.time.LocalDateTime;

public class CaseRelationDto {
    private Long id;
    private Long primaryComplaintId;
    private String primaryComplaintNumber;
    private String primaryTitle;
    private Long secondaryComplaintId;
    private String secondaryComplaintNumber;
    private String secondaryTitle;
    private String relationType;
    private String reason;
    private Double similarityScore;
    private boolean active;
    private String createdByName;
    private LocalDateTime createdAt;
    private String unmergedByName;
    private LocalDateTime unmergedAt;
    private String unmergeReason;

    public CaseRelationDto() {
    }

    public static CaseRelationDto fromEntity(ComplaintRelation rel) {
        if (rel == null) return null;
        CaseRelationDto dto = new CaseRelationDto();
        dto.setId(rel.getId());
        if (rel.getPrimaryComplaint() != null) {
            dto.setPrimaryComplaintId(rel.getPrimaryComplaint().getId());
            dto.setPrimaryComplaintNumber(rel.getPrimaryComplaint().getComplaintNumber());
            dto.setPrimaryTitle(rel.getPrimaryComplaint().getTitle());
        }
        if (rel.getSecondaryComplaint() != null) {
            dto.setSecondaryComplaintId(rel.getSecondaryComplaint().getId());
            dto.setSecondaryComplaintNumber(rel.getSecondaryComplaint().getComplaintNumber());
            dto.setSecondaryTitle(rel.getSecondaryComplaint().getTitle());
        }
        dto.setRelationType(rel.getRelationType() != null ? rel.getRelationType().name() : null);
        dto.setReason(rel.getReason());
        dto.setSimilarityScore(rel.getSimilarityScore());
        dto.setActive(rel.isActive());
        if (rel.getCreatedBy() != null) {
            dto.setCreatedByName(rel.getCreatedBy().getFullName());
        }
        dto.setCreatedAt(rel.getCreatedAt());
        if (rel.getUnmergedBy() != null) {
            dto.setUnmergedByName(rel.getUnmergedBy().getFullName());
        }
        dto.setUnmergedAt(rel.getUnmergedAt());
        dto.setUnmergeReason(rel.getUnmergeReason());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrimaryComplaintId() {
        return primaryComplaintId;
    }

    public void setPrimaryComplaintId(Long primaryComplaintId) {
        this.primaryComplaintId = primaryComplaintId;
    }

    public String getPrimaryComplaintNumber() {
        return primaryComplaintNumber;
    }

    public void setPrimaryComplaintNumber(String primaryComplaintNumber) {
        this.primaryComplaintNumber = primaryComplaintNumber;
    }

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public void setPrimaryTitle(String primaryTitle) {
        this.primaryTitle = primaryTitle;
    }

    public Long getSecondaryComplaintId() {
        return secondaryComplaintId;
    }

    public void setSecondaryComplaintId(Long secondaryComplaintId) {
        this.secondaryComplaintId = secondaryComplaintId;
    }

    public String getSecondaryComplaintNumber() {
        return secondaryComplaintNumber;
    }

    public void setSecondaryComplaintNumber(String secondaryComplaintNumber) {
        this.secondaryComplaintNumber = secondaryComplaintNumber;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUnmergedByName() {
        return unmergedByName;
    }

    public void setUnmergedByName(String unmergedByName) {
        this.unmergedByName = unmergedByName;
    }

    public LocalDateTime getUnmergedAt() {
        return unmergedAt;
    }

    public void setUnmergedAt(LocalDateTime unmergedAt) {
        this.unmergedAt = unmergedAt;
    }

    public String getUnmergeReason() {
        return unmergeReason;
    }

    public void setUnmergeReason(String unmergeReason) {
        this.unmergeReason = unmergeReason;
    }
}
