package com.aicte.grievance.dto;

public class CategoryStatDto {
    private String category;
    private String displayName;
    private long count;

    public CategoryStatDto() {
    }

    public CategoryStatDto(String category, String displayName, long count) {
        this.category = category;
        this.displayName = displayName;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
