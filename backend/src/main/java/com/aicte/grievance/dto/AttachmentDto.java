package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Attachment;
import java.time.LocalDateTime;

public class AttachmentDto {
    private Long id;
    private Long complaintId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    public AttachmentDto() {
    }

    public static AttachmentDto fromEntity(Attachment a) {
        if (a == null) return null;
        AttachmentDto dto = new AttachmentDto();
        dto.setId(a.getId());
        if (a.getComplaint() != null) {
            dto.setComplaintId(a.getComplaint().getId());
        }
        dto.setFileName(a.getFileName());
        dto.setFileType(a.getFileType());
        dto.setFileUrl(a.getFileUrl());
        dto.setFileSize(a.getFileSize());
        if (a.getUploadedBy() != null) {
            dto.setUploadedById(a.getUploadedBy().getId());
            dto.setUploadedByName(a.getUploadedBy().getFullName());
        }
        dto.setUploadedAt(a.getUploadedAt());
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
