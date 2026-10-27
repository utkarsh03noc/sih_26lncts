package com.aicte.grievance.service;

import com.aicte.grievance.dto.*;
import com.aicte.grievance.entity.*;
import com.aicte.grievance.exception.BadRequestException;
import com.aicte.grievance.exception.ResourceNotFoundException;
import com.aicte.grievance.exception.UnauthorizedException;
import com.aicte.grievance.repository.*;
import com.aicte.grievance.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplaintMergeService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintRelationRepository complaintRelationRepository;
    private final ComplaintUpdateRepository complaintUpdateRepository;
    private final AttachmentRepository attachmentRepository;
    private final ComplaintTransferRepository complaintTransferRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final SemanticSimilarityEngine semanticSimilarityEngine;

    @Value("${app.merge.allow-cross-institution:false}")
    private boolean globalAllowCrossInstitutionMerge;

    public ComplaintMergeService(ComplaintRepository complaintRepository,
                                 ComplaintRelationRepository complaintRelationRepository,
                                 ComplaintUpdateRepository complaintUpdateRepository,
                                 AttachmentRepository attachmentRepository,
                                 ComplaintTransferRepository complaintTransferRepository,
                                 UserRepository userRepository,
                                 AuditLogService auditLogService,
                                 DuplicateDetectionService duplicateDetectionService,
                                 SemanticSimilarityEngine semanticSimilarityEngine) {
        this.complaintRepository = complaintRepository;
        this.complaintRelationRepository = complaintRelationRepository;
        this.complaintUpdateRepository = complaintUpdateRepository;
        this.attachmentRepository = attachmentRepository;
        this.complaintTransferRepository = complaintTransferRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.semanticSimilarityEngine = semanticSimilarityEngine;
    }

    /**
     * Approves merging of a secondary complaint into a primary master case.
     * Guaranteed transactional, idempotent, and non-destructive.
     */
    @Transactional
    public UnifiedCaseDto mergeComplaints(ComplaintMergeRequestDto request, UserPrincipal currentUser) {
        validateReviewerAuthorization(currentUser);

        if (request == null || request.getPrimaryComplaintId() == null || request.getSecondaryComplaintId() == null) {
            throw new BadRequestException("Primary master complaint ID and secondary complaint ID are required.");
        }

        if (request.getPrimaryComplaintId().equals(request.getSecondaryComplaintId())) {
            throw new BadRequestException("Self-merge is not allowed. A complaint cannot be merged into itself.");
        }

        if (request.getMergeReason() == null || request.getMergeReason().trim().length() < 10) {
            throw new BadRequestException("Merge approval requires a detailed written justification of at least 10 characters.");
        }

        Complaint primary = complaintRepository.findById(request.getPrimaryComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", request.getPrimaryComplaintId()));

        Complaint secondary = complaintRepository.findById(request.getSecondaryComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", request.getSecondaryComplaintId()));

        // Validate Officer Institution Scope
        validateOfficerInstitutionScope(currentUser, primary, secondary);

        // Same Institution PID Validation
        boolean allowCross = request.isCrossInstitutionAuthorized() || globalAllowCrossInstitutionMerge;
        if (!allowCross && !primary.getInstitution().getId().equals(secondary.getInstitution().getId())) {
            throw new BadRequestException(String.format(
                    "Cross-institution merge is not permitted. Both complaints must belong to the same institution PID (%s vs %s).",
                    primary.getInstitution().getCode(), secondary.getInstitution().getCode()));
        }

        // Secondary must not already be merged
        if (secondary.getStatus() == ComplaintStatus.MERGED) {
            throw new BadRequestException(String.format(
                    "Complaint %s is already merged into another case and cannot be merged again.",
                    secondary.getComplaintNumber()));
        }

        // Primary master must not be a merged secondary case
        if (primary.getStatus() == ComplaintStatus.MERGED) {
            throw new BadRequestException(String.format(
                    "Complaint %s is currently a merged secondary case and cannot be chosen as primary master case.",
                    primary.getComplaintNumber()));
        }

        // Circular merge prevention
        Optional<ComplaintRelation> circular = complaintRelationRepository.findActiveRelation(
                secondary.getId(), primary.getId(), CaseRelationType.MERGED);
        if (circular.isPresent()) {
            throw new BadRequestException(String.format(
                    "Circular merge detected: Complaint %s cannot be merged into %s.",
                    primary.getComplaintNumber(), secondary.getComplaintNumber()));
        }

        // Existing merge check
        Optional<ComplaintRelation> existing = complaintRelationRepository.findActiveRelation(
                primary.getId(), secondary.getId(), CaseRelationType.MERGED);
        if (existing.isPresent()) {
            throw new BadRequestException(String.format(
                    "An active merge relation already exists between %s and %s.",
                    primary.getComplaintNumber(), secondary.getComplaintNumber()));
        }

        User performingUser = userRepository.findById(currentUser.getId()).orElse(null);

        // Compute similarity score if missing
        Double simScore = request.getSimilarityScore();
        if (simScore == null) {
            SemanticSimilarityEngine.SemanticSimilarityResult sim =
                    semanticSimilarityEngine.compareTexts(primary.getTitle(), secondary.getTitle());
            simScore = Math.round(sim.overallSimilarity() * 100.0 * 10.0) / 10.0;
        }

        ComplaintStatus secondaryOriginalStatus = secondary.getStatus();

        // Durably record the relation
        ComplaintRelation relation = new ComplaintRelation(
                primary, secondary, CaseRelationType.MERGED,
                request.getMergeReason().trim(), simScore,
                secondaryOriginalStatus, performingUser
        );
        complaintRelationRepository.save(relation);

        // Update secondary status to MERGED (non-destructive; original description, attachments, submitter untouched)
        secondary.setStatus(ComplaintStatus.MERGED);
        secondary.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(secondary);

        // Log timeline updates
        ComplaintUpdate secondaryUpdate = new ComplaintUpdate(
                secondary,
                secondaryOriginalStatus,
                ComplaintStatus.MERGED,
                String.format("Case officially merged into Master Complaint %s under Unified Case File. Reason: %s",
                        primary.getComplaintNumber(), request.getMergeReason().trim()),
                performingUser
        );
        complaintUpdateRepository.save(secondaryUpdate);

        ComplaintUpdate primaryUpdate = new ComplaintUpdate(
                primary,
                primary.getStatus(),
                primary.getStatus(),
                String.format("Secondary complaint %s (submitted by %s) merged into this Master Case. Reason: %s",
                        secondary.getComplaintNumber(),
                        secondary.getComplainant() != null ? secondary.getComplainant().getFullName() : "Complainant",
                        request.getMergeReason().trim()),
                performingUser
        );
        complaintUpdateRepository.save(primaryUpdate);

        // Durable Audit Log
        auditLogService.log(
                "COMPLAINT_MERGED",
                performingUser,
                "Complaint",
                primary.getId(),
                String.format("Officer-Approved Merge: Merged secondary [%s] into master [%s]. Institution PID [%s]. Reason: %s",
                        secondary.getComplaintNumber(), primary.getComplaintNumber(), primary.getInstitution().getCode(), request.getMergeReason().trim()),
                null
        );

        return getUnifiedCaseFile(primary.getId());
    }

    /**
     * Authorized reversal / unmerge of a secondary complaint from its master case.
     * Faithfully restores secondary status, preserves full merge and unmerge history.
     */
    @Transactional
    public UnifiedCaseDto unmergeComplaints(ComplaintUnmergeRequestDto request, UserPrincipal currentUser) {
        validateReviewerAuthorization(currentUser);

        if (request == null || request.getPrimaryComplaintId() == null || request.getSecondaryComplaintId() == null) {
            throw new BadRequestException("Primary master complaint ID and secondary complaint ID are required.");
        }

        if (request.getUnmergeReason() == null || request.getUnmergeReason().trim().length() < 10) {
            throw new BadRequestException("Unmerge justification requires at least 10 characters for regulatory transparency.");
        }

        Complaint primary = complaintRepository.findById(request.getPrimaryComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", request.getPrimaryComplaintId()));

        Complaint secondary = complaintRepository.findById(request.getSecondaryComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", request.getSecondaryComplaintId()));

        validateOfficerInstitutionScope(currentUser, primary, secondary);

        ComplaintRelation relation = complaintRelationRepository.findActiveRelation(
                primary.getId(), secondary.getId(), CaseRelationType.MERGED)
                .orElseThrow(() -> new BadRequestException(String.format(
                        "No active merge relation found between Master %s and Secondary %s.",
                        primary.getComplaintNumber(), secondary.getComplaintNumber())));

        User performingUser = userRepository.findById(currentUser.getId()).orElse(null);

        // Deactivate merge relation while retaining historical record
        relation.setActive(false);
        relation.setUnmergedBy(performingUser);
        relation.setUnmergedAt(LocalDateTime.now());
        relation.setUnmergeReason(request.getUnmergeReason().trim());
        complaintRelationRepository.save(relation);

        // Restore secondary's prior status
        ComplaintStatus restoredStatus = relation.getPreviousStatus() != null
                ? relation.getPreviousStatus()
                : ComplaintStatus.UNDER_REVIEW;

        secondary.setStatus(restoredStatus);
        secondary.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(secondary);

        // Record updates on timeline
        ComplaintUpdate secondaryUpdate = new ComplaintUpdate(
                secondary,
                ComplaintStatus.MERGED,
                restoredStatus,
                String.format("Case unmerged from Master Complaint %s. Status restored to [%s]. Justification: %s",
                        primary.getComplaintNumber(), restoredStatus, request.getUnmergeReason().trim()),
                performingUser
        );
        complaintUpdateRepository.save(secondaryUpdate);

        ComplaintUpdate primaryUpdate = new ComplaintUpdate(
                primary,
                primary.getStatus(),
                primary.getStatus(),
                String.format("Secondary complaint %s unmerged from this master case. Justification: %s",
                        secondary.getComplaintNumber(), request.getUnmergeReason().trim()),
                performingUser
        );
        complaintUpdateRepository.save(primaryUpdate);

        // Audit Log
        auditLogService.log(
                "COMPLAINT_UNMERGED",
                performingUser,
                "Complaint",
                primary.getId(),
                String.format("Regulatory Reversal: Unmerged secondary [%s] from master [%s]. Status restored to [%s]. Reason: %s",
                        secondary.getComplaintNumber(), primary.getComplaintNumber(), restoredStatus, request.getUnmergeReason().trim()),
                null
        );

        return getUnifiedCaseFile(primary.getId());
    }

    /**
     * Links two complaints as related-but-distinct without changing their statuses.
     */
    @Transactional
    public CaseRelationDto linkAsRelated(CaseLinkRequestDto request, UserPrincipal currentUser) {
        validateReviewerAuthorization(currentUser);

        if (request == null || request.getSourceComplaintId() == null || request.getTargetComplaintId() == null) {
            throw new BadRequestException("Source and target complaint IDs are required.");
        }

        if (request.getSourceComplaintId().equals(request.getTargetComplaintId())) {
            throw new BadRequestException("A complaint cannot be linked to itself.");
        }

        if (request.getLinkReason() == null || request.getLinkReason().trim().length() < 5) {
            throw new BadRequestException("Link reason must be at least 5 characters.");
        }

        Complaint source = complaintRepository.findById(request.getSourceComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", request.getSourceComplaintId()));

        Complaint target = complaintRepository.findById(request.getTargetComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", request.getTargetComplaintId()));

        validateOfficerInstitutionScope(currentUser, source, target);

        List<ComplaintRelation> existing = complaintRelationRepository.findActiveBidirectionalRelations(source.getId(), target.getId());
        if (!existing.isEmpty()) {
            throw new BadRequestException("A relation already exists between these complaints.");
        }

        User performingUser = userRepository.findById(currentUser.getId()).orElse(null);

        ComplaintRelation relation = new ComplaintRelation(
                source, target, CaseRelationType.RELATED,
                request.getLinkReason().trim(), null, null, performingUser
        );
        complaintRelationRepository.save(relation);

        ComplaintUpdate updateA = new ComplaintUpdate(
                source, source.getStatus(), source.getStatus(),
                String.format("Linked as related grievance to case %s. Note: %s", target.getComplaintNumber(), request.getLinkReason().trim()),
                performingUser
        );
        complaintUpdateRepository.save(updateA);

        ComplaintUpdate updateB = new ComplaintUpdate(
                target, target.getStatus(), target.getStatus(),
                String.format("Linked as related grievance to case %s. Note: %s", source.getComplaintNumber(), request.getLinkReason().trim()),
                performingUser
        );
        complaintUpdateRepository.save(updateB);

        auditLogService.log(
                "COMPLAINTS_LINKED_RELATED",
                performingUser,
                "Complaint",
                source.getId(),
                String.format("Linked cases %s and %s as related. Note: %s", source.getComplaintNumber(), target.getComplaintNumber(), request.getLinkReason().trim()),
                null
        );

        return CaseRelationDto.fromEntity(relation);
    }

    /**
     * Finds a complaint by its database ID or complaint number string.
     */
    public Complaint findComplaintByIdOrNumber(String identifier) {
        if (identifier == null || identifier.isBlank() || "undefined".equalsIgnoreCase(identifier)) {
            throw new BadRequestException("Valid complaint identifier is required.");
        }
        try {
            Long id = Long.parseLong(identifier.trim());
            Optional<Complaint> byId = complaintRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        } catch (NumberFormatException ignored) {
        }
        return complaintRepository.findByComplaintNumber(identifier.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "identifier", identifier));
    }

    /**
     * Builds side-by-side comparison between two complaints for review prior to merge.
     */
    @Transactional(readOnly = true)
    public CaseCompareDto compareComplaints(String idA, String idB, UserPrincipal currentUser) {
        Complaint caseA = findComplaintByIdOrNumber(idA);
        Complaint caseB = findComplaintByIdOrNumber(idB);
        return compareComplaints(caseA.getId(), caseB.getId(), currentUser);
    }

    /**
     * Builds side-by-side comparison between two complaints for review prior to merge.
     */
    @Transactional(readOnly = true)
    public CaseCompareDto compareComplaints(Long idA, Long idB, UserPrincipal currentUser) {
        Complaint caseA = complaintRepository.findById(idA)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", idA));
        Complaint caseB = complaintRepository.findById(idB)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", idB));

        CaseCompareDto dto = new CaseCompareDto();
        dto.setCaseA(ComplaintResponseDto.fromEntity(caseA));
        dto.setCaseB(ComplaintResponseDto.fromEntity(caseB));

        boolean sameInst = caseA.getInstitution() != null && caseB.getInstitution() != null &&
                caseA.getInstitution().getId().equals(caseB.getInstitution().getId());
        dto.setSameInstitution(sameInst);

        if (caseA.getInstitution() != null) {
            dto.setInstitutionPidA(caseA.getInstitution().getCode());
            dto.setInstitutionName(caseA.getInstitution().getName());
        }
        if (caseB.getInstitution() != null) {
            dto.setInstitutionPidB(caseB.getInstitution().getCode());
        }

        // Semantic & lexical comparison
        SemanticSimilarityEngine.SemanticSimilarityResult sim =
                semanticSimilarityEngine.compareTexts(caseA.getTitle() + " " + caseA.getDescription(),
                        caseB.getTitle() + " " + caseB.getDescription());
        dto.setSimilarityScore(Math.round(sim.overallSimilarity() * 100.0));
        dto.setMatchType(sim.matchType());
        dto.setMatchedConcepts(sim.sharedConcepts());
        dto.setMatchExplanation(sim.explanation());

        // Check active relations
        List<ComplaintRelation> relations = complaintRelationRepository.findActiveBidirectionalRelations(idA, idB);
        boolean isMerged = relations.stream().anyMatch(r -> r.getRelationType() == CaseRelationType.MERGED);
        boolean isRelated = relations.stream().anyMatch(r -> r.getRelationType() == CaseRelationType.RELATED);
        dto.setAlreadyMerged(isMerged);
        dto.setAlreadyRelated(isRelated);

        if (!relations.isEmpty()) {
            dto.setActiveRelation(CaseRelationDto.fromEntity(relations.get(0)));
        }

        // Determine if merge can be performed
        boolean canMerge = true;
        String disallowReason = null;

        if (idA.equals(idB)) {
            canMerge = false;
            disallowReason = "A complaint cannot be merged into itself.";
        } else if (!sameInst && !globalAllowCrossInstitutionMerge) {
            canMerge = false;
            disallowReason = String.format("Different institution PIDs (%s vs %s). Cross-institution merge disabled.",
                    dto.getInstitutionPidA(), dto.getInstitutionPidB());
        } else if (caseA.getStatus() == ComplaintStatus.MERGED && caseB.getStatus() == ComplaintStatus.MERGED) {
            canMerge = false;
            disallowReason = "Both cases are already merged into other complaints.";
        } else if (isMerged) {
            canMerge = false;
            disallowReason = "These cases are already merged.";
        }

        dto.setCanMerge(canMerge);
        dto.setDisallowReason(disallowReason);

        return dto;
    }

    /**
     * Builds a comprehensive Unified Case File view with consolidated timeline and cross-bureau coordination.
     */
    @Transactional(readOnly = true)
    public UnifiedCaseDto getUnifiedCaseFile(String identifier) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        return getUnifiedCaseFile(complaint.getId());
    }

    /**
     * Builds a comprehensive Unified Case File view with consolidated timeline and cross-bureau coordination.
     */
    @Transactional(readOnly = true)
    public UnifiedCaseDto getUnifiedCaseFile(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        // If this complaint was merged into a master, resolve the master complaint ID
        Long masterId = complaint.getId();
        Optional<ComplaintRelation> mergedInto = complaintRelationRepository.findActiveMergedRelationBySecondaryId(complaint.getId());
        if (mergedInto.isPresent()) {
            masterId = mergedInto.get().getPrimaryComplaint().getId();
            complaint = complaintRepository.findById(masterId).orElse(complaint);
        }

        UnifiedCaseDto dto = new UnifiedCaseDto();
        dto.setUnifiedCaseNumber("UCF-" + complaint.getComplaintNumber());
        dto.setPrimaryComplaintId(complaint.getId());
        dto.setPrimaryComplaint(ComplaintResponseDto.fromEntity(complaint));

        // 1. Fetch active merged secondary complaints
        List<ComplaintRelation> activeMergeRelations = complaintRelationRepository.findActiveMergeRelationsByPrimaryId(masterId);
        List<ComplaintResponseDto> mergedDtos = new ArrayList<>();
        List<Long> allCaseIds = new ArrayList<>();
        allCaseIds.add(complaint.getId());

        for (ComplaintRelation rel : activeMergeRelations) {
            Complaint sec = rel.getSecondaryComplaint();
            ComplaintResponseDto secDto = ComplaintResponseDto.fromEntity(sec);
            secDto.setMerged(true);
            secDto.setMergedIntoComplaintId(complaint.getId());
            secDto.setMergedIntoComplaintNumber(complaint.getComplaintNumber());
            mergedDtos.add(secDto);
            allCaseIds.add(sec.getId());
        }
        dto.setMergedComplaints(mergedDtos);
        dto.setTotalMergedCases(mergedDtos.size());

        // 2. Fetch active related complaints
        List<ComplaintRelation> activeRelatedRelations = complaintRelationRepository.findActiveRelatedRelationsByComplaintId(masterId);
        List<ComplaintResponseDto> relatedDtos = new ArrayList<>();
        for (ComplaintRelation rel : activeRelatedRelations) {
            Complaint other = rel.getPrimaryComplaint().getId().equals(masterId)
                    ? rel.getSecondaryComplaint()
                    : rel.getPrimaryComplaint();
            relatedDtos.add(ComplaintResponseDto.fromEntity(other));
        }
        dto.setRelatedComplaints(relatedDtos);
        dto.setTotalRelatedCases(relatedDtos.size());

        // 3. Consolidated Timeline across master and merged cases
        List<ConsolidatedTimelineItemDto> timeline = new ArrayList<>();

        // Add master creation
        timeline.add(new ConsolidatedTimelineItemDto(
                complaint.getCreatedAt(), "COMPLAINT_CREATED", complaint.getComplaintNumber(),
                complaint.getId(), true, "Grievance Dossier Lodged (Master Case)",
                complaint.getDescription(),
                complaint.getComplainant() != null ? complaint.getComplainant().getFullName() : "Complainant",
                "Complainant"
        ));

        // Add merged cases creations
        for (ComplaintRelation rel : activeMergeRelations) {
            Complaint sec = rel.getSecondaryComplaint();
            timeline.add(new ConsolidatedTimelineItemDto(
                    sec.getCreatedAt(), "COMPLAINT_CREATED", sec.getComplaintNumber(),
                    sec.getId(), false, "Secondary Grievance Filed",
                    sec.getDescription(),
                    sec.getComplainant() != null ? sec.getComplainant().getFullName() : "Complainant",
                    "Complainant"
            ));

            // Add the merge event itself
            timeline.add(new ConsolidatedTimelineItemDto(
                    rel.getCreatedAt(), "CASE_MERGED", sec.getComplaintNumber(),
                    sec.getId(), false,
                    "Officer-Approved Case Merge",
                    String.format("Secondary case %s merged into Master %s. Justification: %s",
                            sec.getComplaintNumber(), complaint.getComplaintNumber(), rel.getReason()),
                    rel.getCreatedBy() != null ? rel.getCreatedBy().getFullName() : "Authorized Officer",
                    "Officer"
            ));
        }

        // Add updates from master and merged cases
        for (Long cId : allCaseIds) {
            Complaint c = complaintRepository.findById(cId).orElse(null);
            if (c == null) continue;
            boolean isMaster = c.getId().equals(masterId);

            List<ComplaintUpdate> updates = complaintUpdateRepository.findByComplaintIdOrderByCreatedAtDesc(cId);
            for (ComplaintUpdate u : updates) {
                timeline.add(new ConsolidatedTimelineItemDto(
                        u.getCreatedAt(), "STATUS_UPDATE", c.getComplaintNumber(),
                        c.getId(), isMaster,
                        "Status Update: " + u.getNewStatus(),
                        u.getRemarks(),
                        u.getUpdatedBy() != null ? u.getUpdatedBy().getFullName() : "System",
                        u.getUpdatedBy() != null ? u.getUpdatedBy().getRole().name() : "SYSTEM"
                ));
            }

            List<ComplaintTransfer> transfers = complaintTransferRepository.findByComplaintIdOrderByTransferredAtDesc(cId);
            for (ComplaintTransfer t : transfers) {
                timeline.add(new ConsolidatedTimelineItemDto(
                        t.getTransferredAt(), "BUREAU_TRANSFER", c.getComplaintNumber(),
                        c.getId(), isMaster,
                        "Bureau / Cell Transfer",
                        String.format("Transferred to [%s / %s]. Reason: %s",
                                t.getToBureau() != null ? t.getToBureau().getName() : "None",
                                t.getToCell() != null ? t.getToCell().getName() : "None",
                                t.getTransferReason()),
                        t.getTransferredBy() != null ? t.getTransferredBy().getFullName() : "Administrator",
                        "Officer"
                ));
            }
        }

        // Chronologically sort timeline (newest first)
        timeline.sort(Comparator.comparing(ConsolidatedTimelineItemDto::getTimestamp).reversed());
        dto.setConsolidatedTimeline(timeline);

        // 4. Cross-Bureau Coordination Panel
        Map<String, BureauCoordinationItemDto> bureauMap = new LinkedHashMap<>();
        if (complaint.getBureau() != null || complaint.getCell() != null) {
            String key = (complaint.getBureau() != null ? complaint.getBureau().getCode() : "NA") + "-" +
                         (complaint.getCell() != null ? complaint.getCell().getCode() : "NA");
            bureauMap.put(key, new BureauCoordinationItemDto(
                    complaint.getBureau() != null ? complaint.getBureau().getCode() : null,
                    complaint.getBureau() != null ? complaint.getBureau().getName() : null,
                    complaint.getCell() != null ? complaint.getCell().getCode() : null,
                    complaint.getCell() != null ? complaint.getCell().getName() : null,
                    complaint.getComplaintNumber(),
                    complaint.getId(),
                    complaint.getAssignedOfficer() != null ? complaint.getAssignedOfficer().getFullName() : "Unassigned",
                    "Primary Master Case Lead"
            ));
        }

        for (ComplaintRelation rel : activeMergeRelations) {
            Complaint sec = rel.getSecondaryComplaint();
            if (sec.getBureau() != null || sec.getCell() != null) {
                String key = (sec.getBureau() != null ? sec.getBureau().getCode() : "NA") + "-" +
                             (sec.getCell() != null ? sec.getCell().getCode() : "NA");
                if (!bureauMap.containsKey(key)) {
                    bureauMap.put(key, new BureauCoordinationItemDto(
                            sec.getBureau() != null ? sec.getBureau().getCode() : null,
                            sec.getBureau() != null ? sec.getBureau().getName() : null,
                            sec.getCell() != null ? sec.getCell().getCode() : null,
                            sec.getCell() != null ? sec.getCell().getName() : null,
                            sec.getComplaintNumber(),
                            sec.getId(),
                            sec.getAssignedOfficer() != null ? sec.getAssignedOfficer().getFullName() : "Unassigned",
                            "Merged Secondary Case Liaison"
                    ));
                }
            }
        }
        dto.setCrossBureauCoordination(new ArrayList<>(bureauMap.values()));

        // 5. Consolidated Attachments
        List<AttachmentDto> allAttachments = new ArrayList<>();
        for (Long cId : allCaseIds) {
            List<Attachment> atts = attachmentRepository.findByComplaintId(cId);
            allAttachments.addAll(atts.stream().map(AttachmentDto::fromEntity).toList());
        }
        dto.setConsolidatedAttachments(allAttachments);

        // 6. Merge History
        List<ComplaintRelation> history = complaintRelationRepository.findAllHistoryByComplaintId(masterId);
        dto.setMergeHistory(history.stream().map(CaseRelationDto::fromEntity).toList());

        return dto;
    }

    /**
     * Finds merge candidates for a given complaint (using similarity engine & same institution cases).
     */
    @Transactional(readOnly = true)
    public List<DuplicateMatchDto> getMergeCandidates(String identifier, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        return getMergeCandidates(complaint.getId(), currentUser);
    }

    /**
     * Finds merge candidates for a given complaint (using similarity engine & same institution cases).
     */
    @Transactional(readOnly = true)
    public List<DuplicateMatchDto> getMergeCandidates(Long complaintId, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        if (isOfficer(currentUser) &&
                (currentUser.getInstitutionId() == null || !currentUser.getInstitutionId().equals(complaint.getInstitution().getId()))) {
            throw new UnauthorizedException("Officers may only access merge candidates for their affiliated institution.");
        }

        List<DuplicateMatchDto> duplicates = duplicateDetectionService.findDuplicates(
                complaint.getTitle(), complaint.getDescription(),
                complaint.getInstitution().getId(), complaint.getCategory()
        );

        // Filter out self and already merged complaints
        return duplicates.stream()
                .filter(d -> !d.getComplaintId().equals(complaint.getId()))
                .filter(d -> d.getStatus() != ComplaintStatus.MERGED)
                .collect(Collectors.toList());
    }

    private boolean isAdmin(UserPrincipal user) {
        return user != null && user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isOfficer(UserPrincipal user) {
        return user != null && user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_OFFICER"));
    }

    private void validateReviewerAuthorization(UserPrincipal currentUser) {
        if (!isAdmin(currentUser) && !isOfficer(currentUser)) {
            throw new UnauthorizedException("Only administrators and institution grievance officers are authorized to approve merges.");
        }
    }

    private void validateOfficerInstitutionScope(UserPrincipal currentUser, Complaint primary, Complaint secondary) {
        if (isOfficer(currentUser)) {
            Long officerInstId = currentUser.getInstitutionId();
            if (officerInstId == null || !officerInstId.equals(primary.getInstitution().getId()) || !officerInstId.equals(secondary.getInstitution().getId())) {
                throw new UnauthorizedException("Institution grievance officers can only merge complaints within their designated institution.");
            }
        }
    }
}
