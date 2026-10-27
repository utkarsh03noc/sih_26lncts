package com.aicte.grievance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_updates")
public class ComplaintUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ComplaintStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus newStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by_id", nullable = false)
    private User updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ComplaintUpdate() {
    }

    public ComplaintUpdate(Complaint complaint, ComplaintStatus previousStatus, ComplaintStatus newStatus, String remarks, User updatedBy) {
        this.complaint = complaint;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.updatedBy = updatedBy;
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

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
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

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
