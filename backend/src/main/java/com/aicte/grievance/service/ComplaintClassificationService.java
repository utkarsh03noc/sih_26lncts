package com.aicte.grievance.service;

import com.aicte.grievance.dto.ComplaintClassificationRequestDto;
import com.aicte.grievance.dto.ComplaintClassificationResponseDto;
import com.aicte.grievance.entity.Bureau;
import com.aicte.grievance.entity.Cell;
import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.Priority;
import com.aicte.grievance.repository.BureauRepository;
import com.aicte.grievance.repository.CellRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Local-First AI-Assisted Complaint Classifier for the AICTE Grievance Portal.
 *
 * Implements a transparent, zero-cloud multi-feature Bayesian and concept-heuristic classifier.
 * Evaluates grievance subject, detailed statements, and urgency factors to suggest:
 * - Category
 * - Priority
 * - Responsible Bureau and Cell
 * - Salient keywords
 * - Human-in-the-loop explanation and confidence indicators
 */
@Service
public class ComplaintClassificationService {

    private final BureauRepository bureauRepository;
    private final CellRepository cellRepository;
    private final SemanticSimilarityEngine semanticSimilarityEngine;

    @Value("${app.ai.classifier.min-confidence-threshold:0.40}")
    private double minConfidenceThreshold;

    private static final String CLASSIFIER_DESCRIPTOR =
            "Rule-Weighted Multi-Feature Bayesian & Domain Classifier (Local-First, Zero-Cloud)";

    // Category feature keywords with relevance weightings
    private static final Map<ComplaintCategory, Map<String, Double>> CATEGORY_FEATURES = new EnumMap<>(ComplaintCategory.class);

    // Priority urgency indicator keywords
    private static final Map<String, Priority> PRIORITY_URGENCY_KEYWORDS = new LinkedHashMap<>();

    static {
        // --- 1. SCHOLARSHIP ---
        Map<String, Double> sch = new HashMap<>();
        sch.put("scholarship", 3.5); sch.put("stipend", 3.5); sch.put("pragati", 4.0); sch.put("saksham", 4.0);
        sch.put("swanath", 4.0); sch.put("fellowship", 3.0); sch.put("grant", 2.5); sch.put("disbursement", 3.0);
        sch.put("disbursed", 2.5); sch.put("nsp", 3.0); sch.put("postmatric", 3.0); sch.put("allowance", 2.5);
        sch.put("aicte pg", 3.5); sch.put("credit", 2.0); sch.put("delayed stipend", 3.5);
        CATEGORY_FEATURES.put(ComplaintCategory.SCHOLARSHIP, sch);

        // --- 2. FEES ---
        Map<String, Double> fee = new HashMap<>();
        fee.put("fee", 3.0); fee.put("fees", 3.0); fee.put("capitation", 4.5); fee.put("tuition", 3.0);
        fee.put("donation", 4.0); fee.put("fine", 2.5); fee.put("fines", 2.5); fee.put("penalty", 2.5);
        fee.put("refund", 3.0); fee.put("caution", 3.5); fee.put("deposit", 3.0); fee.put("overcharge", 3.0);
        fee.put("extortion", 4.0); fee.put("challan", 2.5); fee.put("receipt", 2.0); fee.put("dues", 2.0);
        CATEGORY_FEATURES.put(ComplaintCategory.FEES, fee);

        // --- 3. EXAMINATION ---
        Map<String, Double> exam = new HashMap<>();
        exam.put("exam", 3.0); exam.put("examination", 3.5); exam.put("examinations", 3.5);
        exam.put("timetable", 3.5); exam.put("practical", 2.5); exam.put("reschedule", 3.5);
        exam.put("rescheduling", 3.5); exam.put("hallticket", 4.0); exam.put("admit card", 4.0);
        exam.put("revaluation", 4.0); exam.put("marksheet", 3.5); exam.put("transcript", 3.5);
        exam.put("assessment", 2.5); exam.put("midterm", 3.0); exam.put("answer script", 3.5);
        CATEGORY_FEATURES.put(ComplaintCategory.EXAMINATION, exam);

        // --- 4. HOSTEL ---
        Map<String, Double> hos = new HashMap<>();
        hos.put("hostel", 4.0); hos.put("mess", 3.5); hos.put("canteen", 3.0); hos.put("dormitory", 3.5);
        hos.put("room", 2.0); hos.put("warden", 3.5); hos.put("water shortage", 3.5); hos.put("drinking water", 3.5);
        hos.put("sanitation", 3.0); hos.put("washroom", 3.0); hos.put("toilet", 3.0); hos.put("hygiene", 3.0);
        hos.put("food poisoning", 4.0); hos.put("accommodation", 2.5);
        CATEGORY_FEATURES.put(ComplaintCategory.HOSTEL, hos);

        // --- 5. INFRASTRUCTURE ---
        Map<String, Double> inf = new HashMap<>();
        inf.put("lab", 3.0); inf.put("laboratory", 3.5); inf.put("workstation", 3.0); inf.put("gpu", 3.5);
        inf.put("computer", 2.5); inf.put("equipment", 3.0); inf.put("wifi", 2.5); inf.put("internet", 2.5);
        inf.put("wiring", 3.5); inf.put("electrical", 3.5); inf.put("short circuit", 4.0); inf.put("shock", 4.0);
        inf.put("fire", 3.5); inf.put("extinguisher", 3.5); inf.put("ramp", 3.5); inf.put("elevator", 3.5);
        inf.put("lift", 3.0); inf.put("waterlogging", 3.5); inf.put("drainage", 3.0);
        CATEGORY_FEATURES.put(ComplaintCategory.INFRASTRUCTURE, inf);

        // --- 6. ADMISSION ---
        Map<String, Double> adm = new HashMap<>();
        adm.put("admission", 4.0); adm.put("admissions", 4.0); adm.put("merit", 3.5); adm.put("cutoff", 3.5);
        adm.put("quota", 3.5); adm.put("tfws", 4.0); adm.put("cet", 3.0); adm.put("jee", 3.0);
        adm.put("counseling", 3.0); adm.put("allotment", 3.5); adm.put("cancellation", 3.0);
        adm.put("migration", 3.5); adm.put("sports quota", 4.0); adm.put("withholding certificates", 4.0);
        CATEGORY_FEATURES.put(ComplaintCategory.ADMISSION, adm);

        // --- 7. FACULTY_STAFF ---
        Map<String, Double> fac = new HashMap<>();
        fac.put("faculty", 3.5); fac.put("teacher", 3.0); fac.put("professor", 3.5); fac.put("lecturer", 3.0);
        fac.put("salary", 3.5); fac.put("payroll", 3.5); fac.put("adhoc", 3.5); fac.put("pay scale", 4.0);
        fac.put("7th pay", 4.0); fac.put("maternity", 4.0); fac.put("whistleblower", 4.0);
        fac.put("resignation", 3.5); fac.put("shortage of faculty", 4.0); fac.put("guide", 3.0);
        CATEGORY_FEATURES.put(ComplaintCategory.FACULTY_STAFF, fac);

        // --- 8. HARASSMENT ---
        Map<String, Double> har = new HashMap<>();
        har.put("ragging", 5.0); har.put("harassment", 4.5); har.put("bully", 4.0); har.put("bullying", 4.0);
        har.put("intimidation", 4.0); har.put("threat", 3.5); har.put("threats", 3.5); har.put("humiliation", 4.0);
        har.put("verbal", 3.0); har.put("coercion", 3.5); har.put("icc", 4.0); har.put("dress code", 3.5);
        har.put("cyberbullying", 4.0); har.put("modesty", 4.5); har.put("abuse", 4.0);
        CATEGORY_FEATURES.put(ComplaintCategory.HARASSMENT, har);

        // --- 9. ACADEMIC ---
        Map<String, Double> aca = new HashMap<>();
        aca.put("syllabus", 3.5); aca.put("curriculum", 3.5); aca.put("nep", 4.0); aca.put("elective", 3.5);
        aca.put("credit", 2.5); aca.put("course", 2.0); aca.put("hackathon", 3.5); aca.put("attendance waiver", 4.0);
        aca.put("continuous evaluation", 3.5); aca.put("defense", 3.0); aca.put("synopsis", 3.5);
        CATEGORY_FEATURES.put(ComplaintCategory.ACADEMIC, aca);

        // --- 10. ADMINISTRATION ---
        Map<String, Double> admGov = new HashMap<>();
        admGov.put("bonafide", 4.0); admGov.put("certificate", 3.0); admGov.put("placement", 3.5);
        admGov.put("campus drive", 3.5); admGov.put("club", 3.5); admGov.put("bus", 3.5);
        admGov.put("transit", 3.0); admGov.put("transport", 3.0); admGov.put("library", 2.5);
        admGov.put("registrar", 2.5); admGov.put("fine", 2.0); admGov.put("mass fine", 3.5);
        CATEGORY_FEATURES.put(ComplaintCategory.ADMINISTRATION, admGov);

        // Priority urgency indicators
        PRIORITY_URGENCY_KEYWORDS.put("ragging", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("harassment", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("live wire", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("electrical shock", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("fire safety", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("capitation", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("admission cancellation", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("police", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("suicide", Priority.CRITICAL);
        PRIORITY_URGENCY_KEYWORDS.put("danger", Priority.CRITICAL);

        PRIORITY_URGENCY_KEYWORDS.put("shortage of drinking water", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("water shortage", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("withholding certificates", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("admit card", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("hall ticket", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("scholarship disbursement", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("maternity", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("salary scale", Priority.HIGH);
        PRIORITY_URGENCY_KEYWORDS.put("transit cancelled", Priority.HIGH);

        PRIORITY_URGENCY_KEYWORDS.put("rescheduling", Priority.MEDIUM);
        PRIORITY_URGENCY_KEYWORDS.put("timetable", Priority.MEDIUM);
        PRIORITY_URGENCY_KEYWORDS.put("canteen", Priority.MEDIUM);
        PRIORITY_URGENCY_KEYWORDS.put("internal marks", Priority.MEDIUM);
        PRIORITY_URGENCY_KEYWORDS.put("workstation", Priority.MEDIUM);

        PRIORITY_URGENCY_KEYWORDS.put("bonafide", Priority.LOW);
        PRIORITY_URGENCY_KEYWORDS.put("club activities", Priority.LOW);
        PRIORITY_URGENCY_KEYWORDS.put("vending machine", Priority.LOW);
    }

    public ComplaintClassificationService(BureauRepository bureauRepository,
                                          CellRepository cellRepository,
                                          SemanticSimilarityEngine semanticSimilarityEngine) {
        this.bureauRepository = bureauRepository;
        this.cellRepository = cellRepository;
        this.semanticSimilarityEngine = semanticSimilarityEngine;
    }

    /**
     * Classifies a complaint based on title and description, suggesting Category, Priority, Bureau, and Cell.
     */
    public ComplaintClassificationResponseDto classifyComplaint(ComplaintClassificationRequestDto request) {
        ComplaintClassificationResponseDto response = new ComplaintClassificationResponseDto();
        response.setClassifierType(CLASSIFIER_DESCRIPTOR);

        String title = request != null && request.getTitle() != null ? request.getTitle().trim() : "";
        String desc = request != null && request.getDescription() != null ? request.getDescription().trim() : "";
        String fullText = (title + " " + desc).toLowerCase();

        // 1. Guard against empty or overly short text
        if (fullText.trim().length() < 10) {
            response.setSuggestedCategory(ComplaintCategory.OTHER);
            response.setCategoryDisplayName(ComplaintCategory.OTHER.getDisplayName());
            response.setSuggestedPriority(Priority.MEDIUM);
            response.setConfidence(0.0);
            response.setConfidenceLevel("MANUAL_REVIEW_REQUIRED");
            response.setManualReviewRequired(true);
            response.setExplanation("Manual review required: Grievance description is too brief or ambiguous to reliably determine category and priority.");
            attachBureauAndCell(response, ComplaintCategory.OTHER, Collections.emptySet());
            return response;
        }

        // 2. Extract salient tokens
        Set<String> tokens = semanticSimilarityEngine.extractSalientTokens(fullText);
        response.setExtractedKeywords(tokens.stream().limit(6).collect(Collectors.toList()));

        // 3. Category Scoring
        Map<ComplaintCategory, Double> scores = new EnumMap<>(ComplaintCategory.class);
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            scores.put(cat, 0.0);
        }

        for (Map.Entry<ComplaintCategory, Map<String, Double>> entry : CATEGORY_FEATURES.entrySet()) {
            ComplaintCategory cat = entry.getKey();
            Map<String, Double> features = entry.getValue();

            double catScore = 0.0;
            for (Map.Entry<String, Double> feat : features.entrySet()) {
                String term = feat.getKey();
                double weight = feat.getValue();

                if (fullText.contains(term)) {
                    catScore += weight;
                } else if (tokens.contains(term)) {
                    catScore += weight * 0.8;
                }
            }
            scores.put(cat, catScore);
        }

        // Rank categories by score
        List<Map.Entry<ComplaintCategory, Double>> ranked = scores.entrySet().stream()
                .filter(e -> e.getKey() != ComplaintCategory.OTHER)
                .sorted(Map.Entry.<ComplaintCategory, Double>comparingByValue().reversed())
                .toList();

        Map.Entry<ComplaintCategory, Double> top = ranked.get(0);
        ComplaintCategory chosenCat = top.getKey();
        double topScore = top.getValue();

        // 4. Uncertainty & Threshold Evaluation
        double totalScore = ranked.stream().mapToDouble(Map.Entry::getValue).sum();
        boolean lowConfidence = topScore < 2.5 || (totalScore > 0 && (topScore / totalScore) < 0.28);

        double confidence;
        String confidenceLevel;
        boolean manualReview;

        if (lowConfidence || topScore == 0.0) {
            chosenCat = topScore > 0 ? chosenCat : ComplaintCategory.OTHER;
            confidence = Math.min(0.38, topScore / 10.0);
            confidenceLevel = "MANUAL_REVIEW_REQUIRED";
            manualReview = true;
        } else {
            confidence = Math.min(0.95, Math.max(0.55, 0.50 + (topScore / (totalScore + 1.0)) * 0.45));
            confidenceLevel = confidence >= 0.80 ? "HIGH" : "MEDIUM";
            manualReview = false;
        }

        response.setSuggestedCategory(chosenCat);
        response.setCategoryDisplayName(chosenCat.getDisplayName());
        response.setConfidence(Math.round(confidence * 100.0) / 100.0);
        response.setConfidenceLevel(confidenceLevel);
        response.setManualReviewRequired(manualReview);

        // 5. Priority Urgency Evaluation
        Priority chosenPriority = evaluatePriority(fullText, chosenCat);
        response.setSuggestedPriority(chosenPriority);

        // 6. Bureau and Cell Mapping
        attachBureauAndCell(response, chosenCat, tokens);

        // 7. Transparent Explanation
        String explanation;
        if (manualReview) {
            explanation = "Manual review required: Grievance contains ambiguous or multi-disciplinary terminology. Please verify suggested category and routing.";
        } else {
            explanation = String.format("Suggested category [%s] based on key indicators (%s). Priority [%s] recommended based on urgency indicators.",
                    chosenCat.getDisplayName(),
                    String.join(", ", response.getExtractedKeywords().stream().limit(3).toList()),
                    chosenPriority);
        }
        response.setExplanation(explanation);

        return response;
    }

    private Priority evaluatePriority(String fullText, ComplaintCategory category) {
        // Check critical triggers
        for (Map.Entry<String, Priority> entry : PRIORITY_URGENCY_KEYWORDS.entrySet()) {
            if (entry.getValue() == Priority.CRITICAL && fullText.contains(entry.getKey())) {
                return Priority.CRITICAL;
            }
        }

        // Check high triggers
        for (Map.Entry<String, Priority> entry : PRIORITY_URGENCY_KEYWORDS.entrySet()) {
            if (entry.getValue() == Priority.HIGH && fullText.contains(entry.getKey())) {
                return Priority.HIGH;
            }
        }

        // Check category defaults if no specific trigger
        return switch (category) {
            case HARASSMENT -> Priority.CRITICAL;
            case SCHOLARSHIP, ADMISSION, FEES -> Priority.HIGH;
            case INFRASTRUCTURE, EXAMINATION, FACULTY_STAFF, HOSTEL -> Priority.MEDIUM;
            default -> Priority.LOW;
        };
    }

    private void attachBureauAndCell(ComplaintClassificationResponseDto response, ComplaintCategory category, Set<String> tokens) {
        String bureauCode;
        String cellCode;

        // Check fine-grained statutory cell overrides
        if (tokens.contains("ragging") || tokens.contains("harassment") || tokens.contains("modesty") || category == ComplaintCategory.HARASSMENT) {
            bureauCode = "RB";
            cellCode = "ICC";
        } else if (category == ComplaintCategory.SCHOLARSHIP || category == ComplaintCategory.FEES) {
            bureauCode = "AFB";
            cellCode = "SDC";
        } else if (category == ComplaintCategory.EXAMINATION || category == ComplaintCategory.ACADEMIC) {
            bureauCode = "PAPB";
            cellCode = "CNAC";
        } else if (category == ComplaintCategory.ADMISSION || category == ComplaintCategory.INFRASTRUCTURE) {
            bureauCode = "AB";
            cellCode = "ASC";
        } else if (category == ComplaintCategory.FACULTY_STAFF) {
            bureauCode = "RB";
            cellCode = "ARMC";
        } else {
            bureauCode = "RB";
            cellCode = "PGRC";
        }

        Bureau b = bureauRepository.findByCode(bureauCode).orElse(null);
        if (b != null) {
            response.setSuggestedBureauId(b.getId());
            response.setSuggestedBureauCode(b.getCode());
            response.setSuggestedBureauName(b.getName());
        }

        Cell c = cellRepository.findByCode(cellCode).orElse(null);
        if (c != null) {
            response.setSuggestedCellId(c.getId());
            response.setSuggestedCellCode(c.getCode());
            response.setSuggestedCellName(c.getName());
        }
    }
}
