package com.aicte.grievance.dto;

import java.time.LocalDateTime;

public class ConsolidatedTimelineItemDto {
    private LocalDateTime timestamp;
    private String eventType;
    private String originatingComplaintNumber;
    private Long originatingComplaintId;
    private boolean isPrimaryCase;
    private String title;
    private String description;
    private String actorName;
    private String actorRole;

    public ConsolidatedTimelineItemDto() {
    }

    public ConsolidatedTimelineItemDto(LocalDateTime timestamp, String eventType, String originatingComplaintNumber,
                                       Long originatingComplaintId, boolean isPrimaryCase, String title,
                                       String description, String actorName, String actorRole) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.originatingComplaintNumber = originatingComplaintNumber;
        this.originatingComplaintId = originatingComplaintId;
        this.isPrimaryCase = isPrimaryCase;
        this.title = title;
        this.description = description;
        this.actorName = actorName;
        this.actorRole = actorRole;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOriginatingComplaintNumber() {
        return originatingComplaintNumber;
    }

    public void setOriginatingComplaintNumber(String originatingComplaintNumber) {
        this.originatingComplaintNumber = originatingComplaintNumber;
    }

    public Long getOriginatingComplaintId() {
        return originatingComplaintId;
    }

    public void setOriginatingComplaintId(Long originatingComplaintId) {
        this.originatingComplaintId = originatingComplaintId;
    }

    public boolean isPrimaryCase() {
        return isPrimaryCase;
    }

    public void setPrimaryCase(boolean primaryCase) {
        isPrimaryCase = primaryCase;
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

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }
}
