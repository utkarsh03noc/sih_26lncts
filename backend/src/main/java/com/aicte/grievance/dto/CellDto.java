package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Cell;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class CellDto {
    private Long id;

    @NotBlank(message = "Cell code is required")
    private String code;

    @NotBlank(message = "Cell name is required")
    private String name;

    private String description;
    private Long bureauId;
    private String bureauName;
    private String bureauCode;
    private String contactEmail;
    private boolean active = true;
    private LocalDateTime createdAt;

    public CellDto() {
    }

    public static CellDto fromEntity(Cell c) {
        if (c == null) return null;
        CellDto dto = new CellDto();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        if (c.getBureau() != null) {
            dto.setBureauId(c.getBureau().getId());
            dto.setBureauName(c.getBureau().getName());
            dto.setBureauCode(c.getBureau().getCode());
        }
        dto.setContactEmail(c.getContactEmail());
        dto.setActive(c.isActive());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getBureauId() {
        return bureauId;
    }

    public void setBureauId(Long bureauId) {
        this.bureauId = bureauId;
    }

    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }

    public String getBureauCode() {
        return bureauCode;
    }

    public void setBureauCode(String bureauCode) {
        this.bureauCode = bureauCode;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
