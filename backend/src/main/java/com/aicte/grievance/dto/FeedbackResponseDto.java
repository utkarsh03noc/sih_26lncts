package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Feedback;
import java.time.LocalDateTime;

public class FeedbackResponseDto {
    private Long id;
    private Long complaintId;
    private Long complainantId;
    private String complainantName;
    private Integer rating;
    private String comments;
    private LocalDateTime createdAt;

    public FeedbackResponseDto() {
    }

    public static FeedbackResponseDto fromEntity(Feedback f) {
        if (f == null) return null;
        FeedbackResponseDto dto = new FeedbackResponseDto();
        dto.setId(f.getId());
        if (f.getComplaint() != null) {
            dto.setComplaintId(f.getComplaint().getId());
        }
        if (f.getComplainant() != null) {
            dto.setComplainantId(f.getComplainant().getId());
            dto.setComplainantName(f.getComplainant().getFullName());
        }
        dto.setRating(f.getRating());
        dto.setComments(f.getComments());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public Long getComplainantId() {
        return complainantId;
    }

    public void setComplainantId(Long complainantId) {
        this.complainantId = complainantId;
    }

    public String getComplainantName() {
        return complainantName;
    }

    public void setComplainantName(String complainantName) {
        this.complainantName = complainantName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
