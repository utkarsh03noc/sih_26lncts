package com.aicte.grievance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_transfers")
public class ComplaintTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_bureau_id")
    private Bureau fromBureau;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_bureau_id")
    private Bureau toBureau;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_cell_id")
    private Cell fromCell;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_cell_id")
    private Cell toCell;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_officer_id")
    private User fromOfficer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_officer_id")
    private User toOfficer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String transferReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transferred_by_id", nullable = false)
    private User transferredBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime transferredAt;

    public ComplaintTransfer() {
    }

    public ComplaintTransfer(Complaint complaint, Bureau fromBureau, Bureau toBureau,
                             Cell fromCell, Cell toCell,
                             User fromOfficer, User toOfficer,
                             String transferReason, User transferredBy) {
        this.complaint = complaint;
        this.fromBureau = fromBureau;
        this.toBureau = toBureau;
        this.fromCell = fromCell;
        this.toCell = toCell;
        this.fromOfficer = fromOfficer;
        this.toOfficer = toOfficer;
        this.transferReason = transferReason;
        this.transferredBy = transferredBy;
        this.transferredAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.transferredAt == null) {
            this.transferredAt = LocalDateTime.now();
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

    public Bureau getFromBureau() {
        return fromBureau;
    }

    public void setFromBureau(Bureau fromBureau) {
        this.fromBureau = fromBureau;
    }

    public Bureau getToBureau() {
        return toBureau;
    }

    public void setToBureau(Bureau toBureau) {
        this.toBureau = toBureau;
    }

    public Cell getFromCell() {
        return fromCell;
    }

    public void setFromCell(Cell fromCell) {
        this.fromCell = fromCell;
    }

    public Cell getToCell() {
        return toCell;
    }

    public void setToCell(Cell toCell) {
        this.toCell = toCell;
    }

    public User getFromOfficer() {
        return fromOfficer;
    }

    public void setFromOfficer(User fromOfficer) {
        this.fromOfficer = fromOfficer;
    }

    public User getToOfficer() {
        return toOfficer;
    }

    public void setToOfficer(User toOfficer) {
        this.toOfficer = toOfficer;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public User getTransferredBy() {
        return transferredBy;
    }

    public void setTransferredBy(User transferredBy) {
        this.transferredBy = transferredBy;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }
}
