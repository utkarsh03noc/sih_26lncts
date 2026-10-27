package com.aicte.grievance.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Local-first, zero-cloud semantic similarity engine designed for the AICTE Grievance Portal.
 *
 * Utilizes:
 * 1. AICTE Grievance Concept Ontology (thesaurus clustering for technical education complaints)
 * 2. Concept-expanded vector cosine similarity (detects identical meaning with different wording)
 * 3. Morphological normalization and n-gram Jaccard matching
 * 4. Transparent match explanations (distinguishing semantic similarity from surface keyword overlap)
 */
@Component
public class SemanticSimilarityEngine {

    private static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("[^a-zA-Z0-9\\u0900-\\u097F]+");

    // Standard English & Hinglish Stop Words
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
            "by", "can", "did", "do", "does", "doing", "don", "down", "during", "each", "few", "for",
            "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself",
            "him", "himself", "his", "how", "if", "in", "into", "is", "it", "its", "itself", "just",
            "me", "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once",
            "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "s", "same", "she",
            "should", "so", "some", "such", "than", "that", "the", "their", "theirs", "them", "themselves",
            "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until",
            "up", "very", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom",
            "why", "with", "complaint", "regarding", "issue", "problem", "request", "grievance", "student",
            "college", "institute", "sir", "madam", "please", "kindly", "facing", "also"
    );

    /**
     * AICTE Technical Education Grievance Concept Ontology.
     * Maps synonymous and semantically related terms to centralized concept anchors.
     */
    private static final Map<String, Set<String>> CONCEPT_SYNONYM_CLUSTERS = new LinkedHashMap<>();

    static {
        // 1. Water supply & hygiene
        CONCEPT_SYNONYM_CLUSTERS.put("WATER_SUPPLY_SANITATION", Set.of(
                "water", "drinking", "tap", "pipeline", "pressure", "supply", "shortage", "scarcity",
                "flow", "washroom", "washrooms", "toilet", "toilets", "restroom", "hygiene", "sanitary",
                "sanitation", "cleanliness", "dirty", "unhygienic", "purifier", "filter", "tank", "pumping",
                "pani", "paani"
        ));

        // 2. Hostel & residential facilities
        CONCEPT_SYNONYM_CLUSTERS.put("HOSTEL_AMENITIES", Set.of(
                "hostel", "hostels", "dormitory", "dorm", "room", "rooms", "sharing", "occupancy",
                "bed", "beds", "mess", "canteen", "warden", "amenities", "accommodation", "residence",
                "geyser", "ventilation"
        ));

        // 3. Unauthorized fees & capitation
        CONCEPT_SYNONYM_CLUSTERS.put("UNAUTHORIZED_FEES", Set.of(
                "fee", "fees", "capitation", "tuition", "donation", "development", "bribe", "extortion",
                "payment", "money", "penalty", "fine", "fines", "refund", "deposit", "caution", "overcharge",
                "markup", "receipt", "challan", "dues", "paisa"
        ));

        // 4. Scholarship & financial grants
        CONCEPT_SYNONYM_CLUSTERS.put("SCHOLARSHIP_AID", Set.of(
                "scholarship", "stipend", "grant", "fellowship", "pragati", "saksham", "swanath",
                "postmatric", "nsp", "disbursement", "delayed", "credit", "allowance", "welfare"
        ));

        // 5. Examinations & Evaluation
        CONCEPT_SYNONYM_CLUSTERS.put("EXAMINATION_EVALUATION", Set.of(
                "exam", "examination", "examinations", "practical", "timetable", "schedule", "reschedule",
                "rescheduling", "admit", "hallticket", "ticket", "revaluation", "marksheet", "transcript",
                "evaluation", "internal", "assessment", "detention", "pariksha"
        ));

        // 6. Anti-Ragging & Harassment
        CONCEPT_SYNONYM_CLUSTERS.put("HARASSMENT_RAGGING", Set.of(
                "ragging", "harassment", "bully", "bullying", "verbal", "intimidation", "threat",
                "psychological", "torment", "coercion", "humiliation", "misconduct", "senior", "seniors",
                "cyberbullying", "abuse", "discriminatory"
        ));

        // 7. Faculty & Staff misconduct/deficiency
        CONCEPT_SYNONYM_CLUSTERS.put("FACULTY_DEFICIENCY", Set.of(
                "faculty", "teacher", "teachers", "professor", "professors", "instructor", "lecturer",
                "guide", "supervisor", "teaching", "ratio", "shortage", "payroll", "salary", "maternity",
                "whistleblower", "attendance"
        ));

        // 8. Laboratory & Physical Infrastructure
        CONCEPT_SYNONYM_CLUSTERS.put("LAB_INFRASTRUCTURE", Set.of(
                "lab", "laboratory", "workstation", "workstations", "gpu", "computer", "computers",
                "equipment", "apparatus", "wiring", "electrical", "shock", "fire", "extinguisher",
                "internet", "wifi", "ramp", "elevator", "lift", "accessibility", "disabled"
        ));

        // 9. Admission Malpractices & Quota
        CONCEPT_SYNONYM_CLUSTERS.put("ADMISSION_QUOTA", Set.of(
                "admission", "merit", "cutoff", "quota", "tfws", "cet", "jee", "counseling",
                "cancellation", "migration", "certificate", "retention", "withholding", "allotment"
        ));
    }

    /**
     * Represents the calculated semantic comparison between two texts.
     */
    public record SemanticSimilarityResult(
            double overallSimilarity,
            double conceptCosineSimilarity,
            double jaccardTokenSimilarity,
            List<String> sharedConcepts,
            List<String> sharedKeywords,
            String matchType,
            String explanation
    ) {}

    /**
     * Evaluates semantic and lexical similarity between candidate text and reference text.
     */
    public SemanticSimilarityResult compareTexts(String textA, String textB) {
        if (textA == null || textB == null || textA.isBlank() || textB.isBlank()) {
            return new SemanticSimilarityResult(
                    0.0, 0.0, 0.0, Collections.emptyList(), Collections.emptyList(),
                    "NONE", "Insufficient text for comparison"
            );
        }

        String normA = textA.trim().toLowerCase();
        String normB = textB.trim().toLowerCase();

        // 1. Exact string match
        if (normA.equals(normB)) {
            return new SemanticSimilarityResult(
                    1.0, 1.0, 1.0,
                    extractActivatedConcepts(normA),
                    new ArrayList<>(extractSalientTokens(normA)),
                    "EXACT_MATCH",
                    "Exact match on text"
            );
        }

        // 2. Lexical Token Extraction
        Set<String> tokensA = extractSalientTokens(normA);
        Set<String> tokensB = extractSalientTokens(normB);

        // 3. Jaccard & Stemmed Token Overlap
        double jaccardSim = calculateJaccardSimilarity(tokensA, tokensB);
        List<String> sharedKeywords = new ArrayList<>(tokensA);
        sharedKeywords.retainAll(tokensB);

        // 4. Concept Ontology Vectorization & Cosine Similarity
        Map<String, Double> conceptVectorA = buildConceptVector(normA, tokensA);
        Map<String, Double> conceptVectorB = buildConceptVector(normB, tokensB);
        double conceptCosineSim = calculateCosineSimilarity(conceptVectorA, conceptVectorB);

        List<String> sharedConcepts = new ArrayList<>();
        for (String concept : conceptVectorA.keySet()) {
            if (conceptVectorB.containsKey(concept) && conceptVectorA.get(concept) > 0 && conceptVectorB.get(concept) > 0) {
                sharedConcepts.add(formatConceptName(concept));
            }
        }

        // 5. Hybrid Semantic Weighted Score:
        // Concept Cosine accounts for 65% (meaning), Jaccard Token Overlap accounts for 35% (surface wording)
        double overallSim = (conceptCosineSim * 0.65) + (jaccardSim * 0.35);

        // Determine Match Type
        String matchType;
        String explanation;

        if (conceptCosineSim >= 0.60 && jaccardSim < 0.30) {
            matchType = "SEMANTIC_SIMILARITY";
            explanation = String.format("Semantic similarity detected: Identical grievance concepts [%s] expressed with differing vocabulary (Semantic score: %.0f%%).",
                    String.join(", ", sharedConcepts), conceptCosineSim * 100.0);
        } else if (overallSim >= 0.50) {
            matchType = "SEMANTIC_AND_KEYWORD";
            explanation = String.format("Strong subject overlap: Shared concepts [%s] and keywords [%s] (Match score: %.0f%%).",
                    String.join(", ", sharedConcepts),
                    String.join(", ", sharedKeywords.stream().limit(4).toList()),
                    overallSim * 100.0);
        } else if (jaccardSim > 0.15) {
            matchType = "KEYWORD_OVERLAP";
            explanation = String.format("Shared subject keywords detected: [%s] (Lexical overlap: %.0f%%).",
                    String.join(", ", sharedKeywords.stream().limit(4).toList()),
                    jaccardSim * 100.0);
        } else {
            matchType = "LOW_SIMILARITY";
            explanation = "Minimal topical overlap observed.";
        }

        return new SemanticSimilarityResult(
                overallSim,
                conceptCosineSim,
                jaccardSim,
                sharedConcepts,
                sharedKeywords,
                matchType,
                explanation
        );
    }

    /**
     * Extracts salient tokens removing punctuation and stop-words.
     */
    public Set<String> extractSalientTokens(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        return Arrays.stream(TOKEN_SPLIT_PATTERN.split(text.toLowerCase()))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .map(this::normalizeStem)
                .collect(Collectors.toSet());
    }

    /**
     * Fast suffix normalization for standard English grievance terminology.
     */
    private String normalizeStem(String word) {
        if (word.endsWith("ing") && word.length() > 5) return word.substring(0, word.length() - 3);
        if (word.endsWith("tion") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("ies") && word.length() > 5) return word.substring(0, word.length() - 3) + "y";
        if (word.endsWith("es") && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 3) return word.substring(0, word.length() - 1);
        if (word.endsWith("ed") && word.length() > 4) return word.substring(0, word.length() - 2);
        return word;
    }

    /**
     * Maps text tokens into a weighted concept ontology vector.
     */
    private Map<String, Double> buildConceptVector(String rawText, Set<String> tokens) {
        Map<String, Double> vector = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : CONCEPT_SYNONYM_CLUSTERS.entrySet()) {
            String conceptKey = entry.getKey();
            Set<String> synonymSet = entry.getValue();

            double hits = 0;
            for (String syn : synonymSet) {
                if (tokens.contains(syn) || tokens.contains(normalizeStem(syn))) {
                    hits += 1.0;
                } else if (rawText.contains(syn)) {
                    hits += 0.6;
                }
            }

            if (hits > 0) {
                // Diminishing returns log weighting
                vector.put(conceptKey, 1.0 + Math.log(hits));
            }
        }

        return vector;
    }

    private double calculateCosineSimilarity(Map<String, Double> vecA, Map<String, Double> vecB) {
        if (vecA.isEmpty() || vecB.isEmpty()) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (double v : vecA.values()) {
            normA += v * v;
        }
        for (double v : vecB.values()) {
            normB += v * v;
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;

        for (Map.Entry<String, Double> entry : vecA.entrySet()) {
            String term = entry.getKey();
            if (vecB.containsKey(term)) {
                dotProduct += entry.getValue() * vecB.get(term);
            }
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double calculateJaccardSimilarity(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() || setB.isEmpty()) return 0.0;
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    private List<String> extractActivatedConcepts(String rawText) {
        Set<String> tokens = extractSalientTokens(rawText);
        Map<String, Double> vec = buildConceptVector(rawText, tokens);
        return vec.keySet().stream().map(this::formatConceptName).toList();
    }

    private String formatConceptName(String conceptKey) {
        return switch (conceptKey) {
            case "WATER_SUPPLY_SANITATION" -> "Water Supply & Sanitation";
            case "HOSTEL_AMENITIES" -> "Hostel & Living Amenities";
            case "UNAUTHORIZED_FEES" -> "Tuition & Capitation Fees";
            case "SCHOLARSHIP_AID" -> "Scholarship & Financial Aid";
            case "EXAMINATION_EVALUATION" -> "Examinations & Grading";
            case "HARASSMENT_RAGGING" -> "Anti-Ragging & Code of Conduct";
            case "FACULTY_DEFICIENCY" -> "Faculty Standards & Staffing";
            case "LAB_INFRASTRUCTURE" -> "Laboratory & Physical Equipment";
            case "ADMISSION_QUOTA" -> "Admissions & Quota Compliance";
            default -> conceptKey.replace("_", " ");
        };
    }
}
