package com.aicte.grievance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String complaintNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "complainant_id", nullable = false)
    private User complainant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bureau_id")
    private Bureau bureau;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cell_id")
    private Cell cell;

    @Column(name = "assignment_date")
    private LocalDateTime assignmentDate;

    @Column(name = "assignment_remarks", columnDefinition = "TEXT")
    private String assignmentRemarks;

    @Column(name = "potential_duplicate", nullable = false)
    private boolean potentialDuplicate = false;

    @Column(name = "duplicate_acknowledged", nullable = false)
    private boolean duplicateAcknowledged = false;

    @Column(name = "duplicate_remarks", columnDefinition = "TEXT")
    private String duplicateRemarks;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Complaint() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.dueDate == null) {
            // Default SLA: 15 days
            this.dueDate = LocalDate.now().plusDays(15);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public User getComplainant() {
        return complainant;
    }

    public void setComplainant(User complainant) {
        this.complainant = complainant;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public User getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(User assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
    }

    public Bureau getBureau() {
        return bureau;
    }

    public void setBureau(Bureau bureau) {
        this.bureau = bureau;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public LocalDateTime getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDateTime assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getAssignmentRemarks() {
        return assignmentRemarks;
    }

    public void setAssignmentRemarks(String assignmentRemarks) {
        this.assignmentRemarks = assignmentRemarks;
    }

    public boolean isPotentialDuplicate() {
        return potentialDuplicate;
    }

    public void setPotentialDuplicate(boolean potentialDuplicate) {
        this.potentialDuplicate = potentialDuplicate;
    }

    public boolean isDuplicateAcknowledged() {
        return duplicateAcknowledged;
    }

    public void setDuplicateAcknowledged(boolean duplicateAcknowledged) {
        this.duplicateAcknowledged = duplicateAcknowledged;
    }

    public String getDuplicateRemarks() {
        return duplicateRemarks;
    }

    public void setDuplicateRemarks(String duplicateRemarks) {
        this.duplicateRemarks = duplicateRemarks;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
