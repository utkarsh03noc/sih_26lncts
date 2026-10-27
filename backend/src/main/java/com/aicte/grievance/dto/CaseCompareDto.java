package com.aicte.grievance.dto;

import java.util.ArrayList;
import java.util.List;

public class CaseCompareDto {
    private ComplaintResponseDto caseA;
    private ComplaintResponseDto caseB;
    private boolean sameInstitution;
    private String institutionPidA;
    private String institutionPidB;
    private String institutionName;
    private double similarityScore;
    private String matchType;
    private List<String> matchedConcepts = new ArrayList<>();
    private String matchExplanation;
    private boolean alreadyMerged;
    private boolean alreadyRelated;
    private boolean canMerge;
    private String disallowReason;
    private CaseRelationDto activeRelation;

    public CaseCompareDto() {
    }

    public ComplaintResponseDto getCaseA() {
        return caseA;
    }

    public void setCaseA(ComplaintResponseDto caseA) {
        this.caseA = caseA;
    }

    public ComplaintResponseDto getCaseB() {
        return caseB;
    }

    public void setCaseB(ComplaintResponseDto caseB) {
        this.caseB = caseB;
    }

    public boolean isSameInstitution() {
        return sameInstitution;
    }

    public void setSameInstitution(boolean sameInstitution) {
        this.sameInstitution = sameInstitution;
    }

    public String getInstitutionPidA() {
        return institutionPidA;
    }

    public void setInstitutionPidA(String institutionPidA) {
        this.institutionPidA = institutionPidA;
    }

    public String getInstitutionPidB() {
        return institutionPidB;
    }

    public void setInstitutionPidB(String institutionPidB) {
        this.institutionPidB = institutionPidB;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public List<String> getMatchedConcepts() {
        return matchedConcepts;
    }

    public void setMatchedConcepts(List<String> matchedConcepts) {
        this.matchedConcepts = matchedConcepts;
    }

    public String getMatchExplanation() {
        return matchExplanation;
    }

    public void setMatchExplanation(String matchExplanation) {
        this.matchExplanation = matchExplanation;
    }

    public boolean isAlreadyMerged() {
        return alreadyMerged;
    }

    public void setAlreadyMerged(boolean alreadyMerged) {
        this.alreadyMerged = alreadyMerged;
    }

    public boolean isAlreadyRelated() {
        return alreadyRelated;
    }

    public void setAlreadyRelated(boolean alreadyRelated) {
        this.alreadyRelated = alreadyRelated;
    }

    public boolean isCanMerge() {
        return canMerge;
    }

    public void setCanMerge(boolean canMerge) {
        this.canMerge = canMerge;
    }

    public String getDisallowReason() {
        return disallowReason;
    }

    public void setDisallowReason(String disallowReason) {
        this.disallowReason = disallowReason;
    }

    public CaseRelationDto getActiveRelation() {
        return activeRelation;
    }

    public void setActiveRelation(CaseRelationDto activeRelation) {
        this.activeRelation = activeRelation;
    }
}
