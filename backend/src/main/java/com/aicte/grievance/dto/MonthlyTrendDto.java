package com.aicte.grievance.dto;

public class MonthlyTrendDto {
    private String month;
    private long total;
    private long resolved;

    public MonthlyTrendDto() {
    }

    public MonthlyTrendDto(String month, long total, long resolved) {
        this.month = month;
        this.total = total;
        this.resolved = resolved;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }
}
