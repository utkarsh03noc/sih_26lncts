package com.aicte.grievance.service;

import com.aicte.grievance.dto.DuplicateCheckRequestDto;
import com.aicte.grievance.dto.DuplicateMatchDto;
import com.aicte.grievance.entity.Complaint;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {

    private final ComplaintRepository complaintRepository;
    private final SemanticSimilarityEngine semanticSimilarityEngine;

    @Value("${app.ai.duplicate.institution-match-bonus:30.0}")
    private double institutionMatchBonus;

    @Value("${app.ai.duplicate.category-match-bonus:20.0}")
    private double categoryMatchBonus;

    @Value("${app.ai.duplicate.semantic-threshold:0.50}")
    private double semanticCosineThreshold;

    @Value("${app.ai.duplicate.overall-threshold:45.0}")
    private double overallThreshold;

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
            "why", "with", "complaint", "regarding", "issue", "problem", "request", "grievance", "student", "college"
    );

    private static final Pattern WORD_PATTERN = Pattern.compile("\\W+");

    public DuplicateDetectionService(ComplaintRepository complaintRepository,
                                     SemanticSimilarityEngine semanticSimilarityEngine) {
        this.complaintRepository = complaintRepository;
        this.semanticSimilarityEngine = semanticSimilarityEngine;
    }

    @Transactional(readOnly = true)
    public List<DuplicateMatchDto> findDuplicates(String title, String description, Long institutionId, com.aicte.grievance.entity.ComplaintCategory category) {
        DuplicateCheckRequestDto request = new DuplicateCheckRequestDto(title, description, institutionId, category);
        return checkDuplicates(request);
    }

    @Transactional(readOnly = true)
    public List<DuplicateMatchDto> checkDuplicates(DuplicateCheckRequestDto request) {
        if (request == null || request.getInstitutionId() == null) {
            return Collections.emptyList();
        }

        // Fetch recent/active complaints for the institution
        List<Complaint> candidates = complaintRepository.findByInstitutionId(request.getInstitutionId());
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        String reqTitle = request.getTitle() != null ? request.getTitle().trim() : "";
        String reqDesc = request.getDescription() != null ? request.getDescription().trim() : "";

        Set<String> requestTitleTokens = extractKeywords(reqTitle);
        Set<String> requestDescTokens = extractKeywords(reqDesc);

        double effectiveThreshold = (request.getThreshold() != null && request.getThreshold() > 0)
                ? request.getThreshold()
                : overallThreshold;

        List<DuplicateMatchDto> matches = new ArrayList<>();

        for (Complaint candidate : candidates) {
            List<String> reasons = new ArrayList<>();
            double score = 0.0;
            String matchType = "KEYWORD_OVERLAP";
            double maxSemanticScore = 0.0;
            double titleOverlap = 0.0;
            double descOverlap = 0.0;
            List<String> matchedConcepts = new ArrayList<>();

            // 1. Same institution (baseline prerequisite)
            score += institutionMatchBonus;
            reasons.add("Registered under identical institution (" + candidate.getInstitution().getName() + ")");

            // 2. Same Category
            if (request.getCategory() != null && request.getCategory() == candidate.getCategory()) {
                score += categoryMatchBonus;
                reasons.add("Matches complaint category: " + candidate.getCategory().getDisplayName());
            }

            // 3. Exact match check on title
            if (!reqTitle.isBlank() && candidate.getTitle() != null &&
                    reqTitle.equalsIgnoreCase(candidate.getTitle().trim())) {
                score = 100.0;
                matchType = "EXACT_MATCH";
                reasons.add("Exact match on grievance title");
            } else {
                // 4. Semantic Similarity Engine (Concept Vector Cosine + Synonym Detection)
                SemanticSimilarityEngine.SemanticSimilarityResult titleSemantic =
                        semanticSimilarityEngine.compareTexts(reqTitle, candidate.getTitle());
                SemanticSimilarityEngine.SemanticSimilarityResult descSemantic =
                        semanticSimilarityEngine.compareTexts(reqDesc, candidate.getDescription());

                double titleConceptSim = titleSemantic.conceptCosineSimilarity();
                double descConceptSim = descSemantic.conceptCosineSimilarity();
                maxSemanticScore = Math.max(titleConceptSim, descConceptSim);

                Set<String> allSharedConcepts = new LinkedHashSet<>(titleSemantic.sharedConcepts());
                allSharedConcepts.addAll(descSemantic.sharedConcepts());
                matchedConcepts.addAll(allSharedConcepts);

                if (maxSemanticScore >= semanticCosineThreshold) {
                    // Strong semantic similarity detected even if wording differs
                    double semanticBoost = Math.min(35.0, maxSemanticScore * 38.0);
                    score += semanticBoost;
                    matchType = "SEMANTIC_SIMILARITY";
                    reasons.add(String.format("Semantic similarity (%.0f%%): Shared concepts [%s] despite differing wording",
                            maxSemanticScore * 100.0, String.join(", ", matchedConcepts)));
                }

                // 5. Lexical Token Overlap on Title
                Set<String> candidateTitleTokens = extractKeywords(candidate.getTitle());
                titleOverlap = calculateOverlap(requestTitleTokens, candidateTitleTokens);
                if (titleOverlap > 0.15) {
                    double titleScore = Math.min(25.0, titleOverlap * 35.0);
                    score += titleScore;
                    Set<String> commonWords = new HashSet<>(requestTitleTokens);
                    commonWords.retainAll(candidateTitleTokens);
                    reasons.add("Shared subject keywords: " + String.join(", ", commonWords));
                    if ("KEYWORD_OVERLAP".equals(matchType) && titleOverlap >= 0.50) {
                        matchType = "SEMANTIC_AND_KEYWORD";
                    }
                }

                // 6. Lexical Token Overlap on Description
                Set<String> candidateDescTokens = extractKeywords(candidate.getDescription());
                descOverlap = calculateOverlap(requestDescTokens, candidateDescTokens);
                if (descOverlap > 0.15) {
                    double descScore = Math.min(10.0, descOverlap * 15.0);
                    score += descScore;
                }
            }

            // Prerequisite: A candidate is only a potential duplicate if it has actual topical/subject overlap
            // (Exact title match, Semantic concept alignment, or meaningful lexical token overlap).
            // Complaints that merely share an institution/category without subject overlap are NOT duplicates.
            boolean hasTopicalOverlap = ("EXACT_MATCH".equals(matchType))
                    || (maxSemanticScore >= semanticCosineThreshold)
                    || (titleOverlap > 0.15)
                    || (descOverlap > 0.20);

            if (!hasTopicalOverlap) {
                continue;
            }

            // 7. Open / Active Status Urgency Bonus
            boolean isOpen = candidate.getStatus() != ComplaintStatus.RESOLVED && candidate.getStatus() != ComplaintStatus.CLOSED;
            if (isOpen) {
                score += 5.0;
                reasons.add("Existing case is currently open & active (" + candidate.getStatus() + ")");
            }

            // Cap at 99.0% unless exact match
            if (score > 99.0 && !"EXACT_MATCH".equals(matchType)) {
                score = 99.0;
            }

            // Generate clear explanation
            String explanation = switch (matchType) {
                case "EXACT_MATCH" -> "Identical grievance subject filed previously for this institution.";
                case "SEMANTIC_SIMILARITY" -> String.format("Similar grievance concepts detected across %s with distinct phrasing.",
                        matchedConcepts.isEmpty() ? "institutional areas" : matchedConcepts);
                case "SEMANTIC_AND_KEYWORD" -> String.format("Substantial subject and vocabulary overlap with case %s.",
                        candidate.getComplaintNumber());
                default -> "Partial keyword and institutional overlap with prior registered grievance.";
            };

            // Flag if score is above threshold
            if (score >= effectiveThreshold) {
                matches.add(DuplicateMatchDto.fromComplaint(
                        candidate,
                        score,
                        reasons,
                        matchType,
                        maxSemanticScore * 100.0,
                        matchedConcepts,
                        explanation
                ));
            }
        }

        // Sort by similarity score descending
        matches.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        return matches;
    }

    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(WORD_PATTERN.split(text.toLowerCase()))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }

    private double calculateOverlap(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        if (intersection.isEmpty()) return 0.0;
        return (double) intersection.size() / Math.min(set1.size(), set2.size());
    }
}
