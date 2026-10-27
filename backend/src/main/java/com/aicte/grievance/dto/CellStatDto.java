package com.aicte.grievance.dto;

public class CellStatDto {
    private Long cellId;
    private String cellName;
    private String cellCode;
    private long totalCount;

    public CellStatDto() {
    }

    public CellStatDto(Long cellId, String cellName, String cellCode, long totalCount) {
        this.cellId = cellId;
        this.cellName = cellName;
        this.cellCode = cellCode;
        this.totalCount = totalCount;
    }

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getCellCode() {
        return cellCode;
    }

    public void setCellCode(String cellCode) {
        this.cellCode = cellCode;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
