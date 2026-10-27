package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Bureau;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class BureauDto {
    private Long id;

    @NotBlank(message = "Bureau code is required")
    private String code;

    @NotBlank(message = "Bureau name is required")
    private String name;

    private String description;
    private String contactEmail;
    private boolean active = true;
    private int cellCount;
    private LocalDateTime createdAt;

    public BureauDto() {
    }

    public static BureauDto fromEntity(Bureau b) {
        if (b == null) return null;
        BureauDto dto = new BureauDto();
        dto.setId(b.getId());
        dto.setCode(b.getCode());
        dto.setName(b.getName());
        dto.setDescription(b.getDescription());
        dto.setContactEmail(b.getContactEmail());
        dto.setActive(b.isActive());
        dto.setCellCount(b.getCells() != null ? b.getCells().size() : 0);
        dto.setCreatedAt(b.getCreatedAt());
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

    public int getCellCount() {
        return cellCount;
    }

    public void setCellCount(int cellCount) {
        this.cellCount = cellCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
