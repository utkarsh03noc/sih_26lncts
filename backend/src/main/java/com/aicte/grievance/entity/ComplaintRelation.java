package com.aicte.grievance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Durably records officer-approved complaint merges and "Link as Related" relationships.
 * Preserves original records, IDs, full audit attribution, and supports safe unmerging.
 */
@Entity
@Table(name = "case_relations")
public class ComplaintRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_complaint_id", nullable = false)
    private Complaint primaryComplaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secondary_complaint_id", nullable = false)
    private Complaint secondaryComplaint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CaseRelationType relationType = CaseRelationType.MERGED;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private ComplaintStatus previousStatus;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unmerged_by_id")
    private User unmergedBy;

    @Column(name = "unmerged_at")
    private LocalDateTime unmergedAt;

    @Column(name = "unmerge_reason", columnDefinition = "TEXT")
    private String unmergeReason;

    public ComplaintRelation() {
    }

    public ComplaintRelation(Complaint primaryComplaint, Complaint secondaryComplaint,
                             CaseRelationType relationType, String reason, Double similarityScore,
                             ComplaintStatus previousStatus, User createdBy) {
        this.primaryComplaint = primaryComplaint;
        this.secondaryComplaint = secondaryComplaint;
        this.relationType = relationType;
        this.reason = reason;
        this.similarityScore = similarityScore;
        this.previousStatus = previousStatus;
        this.createdBy = createdBy;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getPrimaryComplaint() {
        return primaryComplaint;
    }

    public void setPrimaryComplaint(Complaint primaryComplaint) {
        this.primaryComplaint = primaryComplaint;
    }

    public Complaint getSecondaryComplaint() {
        return secondaryComplaint;
    }

    public void setSecondaryComplaint(Complaint secondaryComplaint) {
        this.secondaryComplaint = secondaryComplaint;
    }

    public CaseRelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(CaseRelationType relationType) {
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

    public ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUnmergedBy() {
        return unmergedBy;
    }

    public void setUnmergedBy(User unmergedBy) {
        this.unmergedBy = unmergedBy;
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
