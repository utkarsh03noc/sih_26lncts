package com.aicte.grievance.dto;

import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.Priority;

import java.util.ArrayList;
import java.util.List;

public class ComplaintClassificationResponseDto {

    private ComplaintCategory suggestedCategory;
    private String categoryDisplayName;
    private Priority suggestedPriority;
    private Long suggestedBureauId;
    private String suggestedBureauCode;
    private String suggestedBureauName;
    private Long suggestedCellId;
    private String suggestedCellCode;
    private String suggestedCellName;
    private List<String> extractedKeywords = new ArrayList<>();
    private String explanation;
    private double confidence;
    private String confidenceLevel; // HIGH, MEDIUM, LOW, MANUAL_REVIEW_REQUIRED
    private boolean manualReviewRequired;
    private String classifierType;

    public ComplaintClassificationResponseDto() {
    }

    public ComplaintCategory getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(ComplaintCategory suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public Priority getSuggestedPriority() {
        return suggestedPriority;
    }

    public void setSuggestedPriority(Priority suggestedPriority) {
        this.suggestedPriority = suggestedPriority;
    }

    public Long getSuggestedBureauId() {
        return suggestedBureauId;
    }

    public void setSuggestedBureauId(Long suggestedBureauId) {
        this.suggestedBureauId = suggestedBureauId;
    }

    public String getSuggestedBureauCode() {
        return suggestedBureauCode;
    }

    public void setSuggestedBureauCode(String suggestedBureauCode) {
        this.suggestedBureauCode = suggestedBureauCode;
    }

    public String getSuggestedBureauName() {
        return suggestedBureauName;
    }

    public void setSuggestedBureauName(String suggestedBureauName) {
        this.suggestedBureauName = suggestedBureauName;
    }

    public Long getSuggestedCellId() {
        return suggestedCellId;
    }

    public void setSuggestedCellId(Long suggestedCellId) {
        this.suggestedCellId = suggestedCellId;
    }

    public String getSuggestedCellCode() {
        return suggestedCellCode;
    }

    public void setSuggestedCellCode(String suggestedCellCode) {
        this.suggestedCellCode = suggestedCellCode;
    }

    public String getSuggestedCellName() {
        return suggestedCellName;
    }

    public void setSuggestedCellName(String suggestedCellName) {
        this.suggestedCellName = suggestedCellName;
    }

    public List<String> getExtractedKeywords() {
        return extractedKeywords;
    }

    public void setExtractedKeywords(List<String> extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public boolean isManualReviewRequired() {
        return manualReviewRequired;
    }

    public void setManualReviewRequired(boolean manualReviewRequired) {
        this.manualReviewRequired = manualReviewRequired;
    }

    public String getClassifierType() {
        return classifierType;
    }

    public void setClassifierType(String classifierType) {
        this.classifierType = classifierType;
    }
}
