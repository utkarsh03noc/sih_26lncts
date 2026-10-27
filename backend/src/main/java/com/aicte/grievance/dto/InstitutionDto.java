package com.aicte.grievance.dto;

import com.aicte.grievance.entity.Institution;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class InstitutionDto {
    private Long id;

    @NotBlank(message = "Institution name is required")
    private String name;

    @NotBlank(message = "Institution code is required")
    private String code;

    @NotBlank(message = "Location is required")
    private String location;

    private String state;
    private String district;
    private String address;
    private String aicteReferenceCode;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    private boolean active = true;
    private LocalDateTime createdAt;
    private long totalComplaints;
    private long openComplaints;
    private long resolvedComplaints;
    private long overdueComplaints;
    private int involvedBureausCount;
    private int involvedCellsCount;

    public InstitutionDto() {
    }

    public static InstitutionDto fromEntity(Institution inst) {
        if (inst == null) return null;
        InstitutionDto dto = new InstitutionDto();
        dto.setId(inst.getId());
        dto.setName(inst.getName());
        dto.setCode(inst.getCode());
        dto.setLocation(inst.getLocation());
        dto.setState(inst.getState());
        dto.setDistrict(inst.getDistrict());
        dto.setAddress(inst.getAddress());
        dto.setAicteReferenceCode(inst.getAicteReferenceCode());
        dto.setType(inst.getType());
        dto.setContactEmail(inst.getContactEmail());
        dto.setContactPhone(inst.getContactPhone());
        dto.setActive(inst.isActive());
        dto.setCreatedAt(inst.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAicteReferenceCode() {
        return aicteReferenceCode;
    }

    public void setAicteReferenceCode(String aicteReferenceCode) {
        this.aicteReferenceCode = aicteReferenceCode;
    }

    public long getOpenComplaints() {
        return openComplaints;
    }

    public void setOpenComplaints(long openComplaints) {
        this.openComplaints = openComplaints;
    }

    public long getOverdueComplaints() {
        return overdueComplaints;
    }

    public void setOverdueComplaints(long overdueComplaints) {
        this.overdueComplaints = overdueComplaints;
    }

    public int getInvolvedBureausCount() {
        return involvedBureausCount;
    }

    public void setInvolvedBureausCount(int involvedBureausCount) {
        this.involvedBureausCount = involvedBureausCount;
    }

    public int getInvolvedCellsCount() {
        return involvedCellsCount;
    }

    public void setInvolvedCellsCount(int involvedCellsCount) {
        this.involvedCellsCount = involvedCellsCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }
}
