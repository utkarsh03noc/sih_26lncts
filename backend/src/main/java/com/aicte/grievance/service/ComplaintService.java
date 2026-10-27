package com.aicte.grievance.service;

import com.aicte.grievance.dto.*;
import com.aicte.grievance.entity.*;
import com.aicte.grievance.exception.BadRequestException;
import com.aicte.grievance.exception.ResourceNotFoundException;
import com.aicte.grievance.exception.UnauthorizedException;
import com.aicte.grievance.repository.*;
import com.aicte.grievance.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintUpdateRepository complaintUpdateRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final AttachmentRepository attachmentRepository;
    private final BureauRepository bureauRepository;
    private final CellRepository cellRepository;
    private final ComplaintTransferRepository complaintTransferRepository;
    private final DuplicateDetectionService duplicateDetectionService;
    private final AuditLogService auditLogService;
    private final ComplaintRelationRepository complaintRelationRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintUpdateRepository complaintUpdateRepository,
                            InstitutionRepository institutionRepository,
                            UserRepository userRepository,
                            FeedbackRepository feedbackRepository,
                            AttachmentRepository attachmentRepository,
                            BureauRepository bureauRepository,
                            CellRepository cellRepository,
                            ComplaintTransferRepository complaintTransferRepository,
                            DuplicateDetectionService duplicateDetectionService,
                            AuditLogService auditLogService,
                            ComplaintRelationRepository complaintRelationRepository) {
        this.complaintRepository = complaintRepository;
        this.complaintUpdateRepository = complaintUpdateRepository;
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.feedbackRepository = feedbackRepository;
        this.attachmentRepository = attachmentRepository;
        this.bureauRepository = bureauRepository;
        this.cellRepository = cellRepository;
        this.complaintTransferRepository = complaintTransferRepository;
        this.duplicateDetectionService = duplicateDetectionService;
        this.auditLogService = auditLogService;
        this.complaintRelationRepository = complaintRelationRepository;
    }

    @Transactional
    public ComplaintResponseDto createComplaint(ComplaintCreateDto dto, UserPrincipal currentUser) {
        User complainant = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Institution institution = institutionRepository.findById(dto.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", dto.getInstitutionId()));

        Complaint complaint = new Complaint();
        String generatedNumber = "CMP-" + Year.now().getValue() + "-" + (1000 + (int)(Math.random() * 9000));
        while (complaintRepository.findByComplaintNumber(generatedNumber).isPresent()) {
            generatedNumber = "CMP-" + Year.now().getValue() + "-" + (1000 + (int)(Math.random() * 9000));
        }

        complaint.setComplaintNumber(generatedNumber);
        complaint.setTitle(dto.getTitle());
        complaint.setDescription(dto.getDescription());
        complaint.setCategory(dto.getCategory());
        complaint.setPriority(dto.getPriority());
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setComplainant(complainant);
        complaint.setInstitution(institution);

        // Optional initial Bureau & Cell
        if (dto.getBureauId() != null) {
            Bureau bureau = bureauRepository.findById(dto.getBureauId()).orElse(null);
            complaint.setBureau(bureau);
        }
        if (dto.getCellId() != null) {
            Cell cell = cellRepository.findById(dto.getCellId()).orElse(null);
            complaint.setCell(cell);
        }

        // Rule-based duplicate / overlap detection
        List<DuplicateMatchDto> potentialMatches = duplicateDetectionService.findDuplicates(
                dto.getTitle(),
                dto.getDescription(),
                dto.getInstitutionId(),
                dto.getCategory()
        );

        if (!potentialMatches.isEmpty()) {
            complaint.setPotentialDuplicate(true);
            if (dto.isDuplicateAcknowledged()) {
                complaint.setDuplicateAcknowledged(true);
                complaint.setDuplicateRemarks(dto.getDuplicateRemarks());
            }
        }

        // Calculate SLA due date based on priority
        int slaDays = switch (dto.getPriority()) {
            case CRITICAL -> 3;
            case HIGH -> 7;
            case MEDIUM -> 15;
            case LOW -> 30;
        };
        complaint.setDueDate(LocalDate.now().plusDays(slaDays));

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Initial timeline entry
        String initialRemarks = "Grievance officially submitted into AICTE monitoring system.";
        if (complaint.isPotentialDuplicate()) {
            initialRemarks += " [Potential overlap/duplicate detected (" + potentialMatches.size() + " matches)]";
            if (complaint.isDuplicateAcknowledged()) {
                initialRemarks += " (Complainant confirmed submission with remarks: " + complaint.getDuplicateRemarks() + ")";
            }
        }

        ComplaintUpdate initialUpdate = new ComplaintUpdate(
                savedComplaint,
                null,
                ComplaintStatus.SUBMITTED,
                initialRemarks,
                complainant
        );
        complaintUpdateRepository.save(initialUpdate);

        auditLogService.log("COMPLAINT_CREATED", complainant, "Complaint", savedComplaint.getId(),
                "Complaint filed: " + savedComplaint.getComplaintNumber() + " against " + institution.getName() +
                        (complaint.isPotentialDuplicate() ? " [Overlap Flagged]" : ""), null);

        return mapToDto(savedComplaint);
    }

    public Complaint findComplaintByIdOrNumber(String idOrNumber) {
        if (idOrNumber == null || idOrNumber.isBlank()) {
            throw new BadRequestException("Complaint ID or complaint number must be provided");
        }
        try {
            Long id = Long.parseLong(idOrNumber);
            java.util.Optional<Complaint> byId = complaintRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        } catch (NumberFormatException ignored) {
        }
        return complaintRepository.findByComplaintNumber(idOrNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "identifier", idOrNumber));
    }

    @Transactional(readOnly = true)
    public ComplaintResponseDto getComplaintById(String identifier, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        validateAccess(complaint, currentUser);
        return mapToDto(complaint);
    }

    @Transactional(readOnly = true)
    public ComplaintResponseDto getComplaintById(Long id, UserPrincipal currentUser) {
        return getComplaintById(String.valueOf(id), currentUser);
    }

    @Transactional(readOnly = true)
    public ComplaintResponseDto getComplaintByNumber(String complaintNumber) {
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "complaintNumber", complaintNumber));
        return mapToDto(complaint);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponseDto> getFilteredComplaints(
            ComplaintStatus status,
            ComplaintCategory category,
            Priority priority,
            Long institutionId,
            Long bureauId,
            Long cellId,
            String search,
            UserPrincipal currentUser,
            Pageable pageable) {

        Long complainantId = null;
        Long officerId = null;

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOfficer = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_OFFICER"));
        boolean isComplainant = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPLAINANT"));

        if (isComplainant) {
            complainantId = currentUser.getId();
        } else if (isOfficer) {
            institutionId = currentUser.getInstitutionId();
        }

        Page<Complaint> page = complaintRepository.findFilteredComplaints(
                status, category, priority, institutionId, bureauId, cellId, complainantId, officerId, search, pageable);

        List<ComplaintResponseDto> dtos = page.getContent().stream()
                .map(this::mapToDto)
                .toList();

        return PagedResponse.of(page, dtos);
    }

    @Transactional
    public ComplaintResponseDto updateComplaintStatus(String identifier, ComplaintStatusUpdateDto updateDto, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        validateAccess(complaint, currentUser);

        ComplaintStatus previousStatus = complaint.getStatus();
        ComplaintStatus newStatus = updateDto.getNewStatus();

        if (previousStatus == newStatus) {
            throw new BadRequestException("Complaint is already in status: " + newStatus);
        }

        complaint.setStatus(newStatus);
        if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.CLOSED) {
            if (complaint.getResolvedAt() == null) {
                complaint.setResolvedAt(LocalDateTime.now());
            }
        }

        Complaint saved = complaintRepository.save(complaint);

        // Record update in timeline
        ComplaintUpdate update = new ComplaintUpdate(
                saved,
                previousStatus,
                newStatus,
                updateDto.getRemarks(),
                user
        );
        complaintUpdateRepository.save(update);

        auditLogService.log("COMPLAINT_STATUS_UPDATED", user, "Complaint", saved.getId(),
                "Status updated from " + previousStatus + " to " + newStatus + ". Remarks: " + updateDto.getRemarks(), null);

        return mapToDto(saved);
    }

    @Transactional
    public ComplaintResponseDto updateComplaintStatus(Long id, ComplaintStatusUpdateDto updateDto, UserPrincipal currentUser) {
        return updateComplaintStatus(String.valueOf(id), updateDto, currentUser);
    }

    @Transactional
    public ComplaintResponseDto assignComplaint(String identifier, ComplaintAssignDto assignDto, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        User officer = userRepository.findById(assignDto.getOfficerId())
                .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", assignDto.getOfficerId()));

        if (officer.getRole() != Role.INSTITUTION_OFFICER && officer.getRole() != Role.ADMIN) {
            throw new BadRequestException("Target user is not an institution officer or administrator");
        }

        if (assignDto.getInstitutionId() != null) {
            Institution inst = institutionRepository.findById(assignDto.getInstitutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", assignDto.getInstitutionId()));
            complaint.setInstitution(inst);
        }

        if (assignDto.getBureauId() != null) {
            Bureau bureau = bureauRepository.findById(assignDto.getBureauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", assignDto.getBureauId()));
            complaint.setBureau(bureau);
        }

        if (assignDto.getCellId() != null) {
            Cell cell = cellRepository.findById(assignDto.getCellId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cell", "id", assignDto.getCellId()));
            complaint.setCell(cell);
        }

        complaint.setAssignedOfficer(officer);
        complaint.setAssignmentDate(LocalDateTime.now());
        complaint.setAssignmentRemarks(assignDto.getRemarks());

        ComplaintStatus prevStatus = complaint.getStatus();
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }

        Complaint saved = complaintRepository.save(complaint);

        String remarks = (assignDto.getRemarks() != null && !assignDto.getRemarks().isBlank())
                ? assignDto.getRemarks()
                : "Complaint assigned to officer " + officer.getFullName();

        ComplaintUpdate update = new ComplaintUpdate(
                saved,
                prevStatus,
                saved.getStatus(),
                remarks,
                user
        );
        complaintUpdateRepository.save(update);

        auditLogService.log("COMPLAINT_ASSIGNED", user, "Complaint", saved.getId(),
                "Assigned to " + officer.getFullName() + " (" + officer.getEmail() + ")", null);

        return mapToDto(saved);
    }

    @Transactional
    public ComplaintResponseDto assignComplaint(Long id, ComplaintAssignDto assignDto, UserPrincipal currentUser) {
        return assignComplaint(String.valueOf(id), assignDto, currentUser);
    }

    @Transactional
    public ComplaintResponseDto transferComplaint(String identifier, ComplaintTransferRequestDto transferDto, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOfficer = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_OFFICER"));

        if (!isAdmin && !isOfficer) {
            throw new UnauthorizedException("Only administrators or authorized officers can transfer cases across Bureaus/Cells");
        }

        Bureau prevBureau = complaint.getBureau();
        Cell prevCell = complaint.getCell();
        User prevOfficer = complaint.getAssignedOfficer();

        Bureau newBureau = prevBureau;
        if (transferDto.getTargetBureauId() != null) {
            newBureau = bureauRepository.findById(transferDto.getTargetBureauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", transferDto.getTargetBureauId()));
        }

        Cell newCell = prevCell;
        if (transferDto.getTargetCellId() != null) {
            newCell = cellRepository.findById(transferDto.getTargetCellId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cell", "id", transferDto.getTargetCellId()));
        }

        User newOfficer = prevOfficer;
        if (transferDto.getTargetOfficerId() != null) {
            newOfficer = userRepository.findById(transferDto.getTargetOfficerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", transferDto.getTargetOfficerId()));
        }

        ComplaintTransfer transfer = new ComplaintTransfer(
                complaint,
                prevBureau,
                newBureau,
                prevCell,
                newCell,
                prevOfficer,
                newOfficer,
                transferDto.getTransferReason(),
                user
        );
        complaintTransferRepository.save(transfer);

        // Update complaint state
        complaint.setBureau(newBureau);
        complaint.setCell(newCell);
        complaint.setAssignedOfficer(newOfficer);
        complaint.setAssignmentDate(LocalDateTime.now());
        complaint.setAssignmentRemarks("Transferred: " + transferDto.getTransferReason());

        ComplaintStatus prevStatus = complaint.getStatus();
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }

        Complaint saved = complaintRepository.save(complaint);

        String transferSummary = "Case transferred";
        if (prevBureau != null || newBureau != null) {
            transferSummary += " from Bureau [" + (prevBureau != null ? prevBureau.getName() : "None") + "] to [" + (newBureau != null ? newBureau.getName() : "None") + "]";
        }
        if (prevCell != null || newCell != null) {
            transferSummary += " / Cell [" + (prevCell != null ? prevCell.getName() : "None") + "] to [" + (newCell != null ? newCell.getName() : "None") + "]";
        }
        transferSummary += ". Reason: " + transferDto.getTransferReason();
        if (transferDto.getRemarks() != null && !transferDto.getRemarks().isBlank()) {
            transferSummary += " (Notes: " + transferDto.getRemarks() + ")";
        }

        ComplaintUpdate timelineUpdate = new ComplaintUpdate(
                saved,
                prevStatus,
                saved.getStatus(),
                transferSummary,
                user
        );
        complaintUpdateRepository.save(timelineUpdate);

        auditLogService.log("CASE_TRANSFERRED", user, "Complaint", saved.getId(), transferSummary, null);

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintTransferDto> getComplaintTransfers(String identifier, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        validateAccess(complaint, currentUser);

        return complaintTransferRepository.findByComplaintIdOrderByTransferredAtDesc(complaint.getId()).stream()
                .map(ComplaintTransferDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DuplicateMatchDto> checkDuplicates(DuplicateCheckRequestDto request) {
        return duplicateDetectionService.findDuplicates(
                request.getTitle(),
                request.getDescription(),
                request.getInstitutionId(),
                request.getCategory()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponseDto> getPotentialDuplicates(Pageable pageable) {
        Page<Complaint> page = complaintRepository.findByPotentialDuplicateTrueOrderByCreatedAtDesc(pageable);
        List<ComplaintResponseDto> dtos = page.getContent().stream()
                .map(this::mapToDto)
                .toList();
        return PagedResponse.of(page, dtos);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getComplaintsByInstitution(Long institutionId, UserPrincipal currentUser) {
        List<Complaint> complaints = complaintRepository.findByInstitutionIdOrderByCreatedAtDesc(institutionId);
        return complaints.stream().map(this::mapToDto).toList();
    }

    @Transactional
    public ComplaintResponseDto reopenComplaint(String identifier, String remarks, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BadRequestException("Only resolved or closed complaints can be reopened");
        }

        ComplaintStatus prevStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.REOPENED);
        complaint.setDueDate(LocalDate.now().plusDays(7)); // Extended SLA for reopened cases

        Complaint saved = complaintRepository.save(complaint);

        ComplaintUpdate update = new ComplaintUpdate(
                saved,
                prevStatus,
                ComplaintStatus.REOPENED,
                (remarks != null && !remarks.isBlank()) ? remarks : "Complaint reopened by user for re-investigation.",
                user
        );
        complaintUpdateRepository.save(update);

        auditLogService.log("COMPLAINT_REOPENED", user, "Complaint", saved.getId(),
                "Reopened grievance: " + saved.getComplaintNumber(), null);

        return mapToDto(saved);
    }

    @Transactional
    public ComplaintResponseDto reopenComplaint(Long id, String remarks, UserPrincipal currentUser) {
        return reopenComplaint(String.valueOf(id), remarks, currentUser);
    }

    @Transactional(readOnly = true)
    public List<ComplaintUpdateDto> getComplaintUpdates(String identifier, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        validateAccess(complaint, currentUser);

        return complaintUpdateRepository.findByComplaintIdOrderByCreatedAtAsc(complaint.getId()).stream()
                .map(ComplaintUpdateDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComplaintUpdateDto> getComplaintUpdates(Long complaintId, UserPrincipal currentUser) {
        return getComplaintUpdates(String.valueOf(complaintId), currentUser);
    }

    @Transactional(readOnly = true)
    public List<AttachmentDto> getComplaintAttachments(String identifier, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        validateAccess(complaint, currentUser);

        return attachmentRepository.findByComplaintId(complaint.getId()).stream()
                .map(AttachmentDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttachmentDto> getComplaintAttachments(Long complaintId, UserPrincipal currentUser) {
        return getComplaintAttachments(String.valueOf(complaintId), currentUser);
    }

    @Transactional
    public FeedbackResponseDto submitFeedback(String identifier, FeedbackCreateDto feedbackDto, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (!complaint.getComplainant().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the original complainant can submit feedback for this complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be submitted after complaint resolution");
        }

        if (feedbackRepository.existsByComplaintId(complaint.getId())) {
            throw new BadRequestException("Feedback has already been submitted for this complaint");
        }

        Feedback feedback = new Feedback(complaint, user, feedbackDto.getRating(), feedbackDto.getComments());
        Feedback saved = feedbackRepository.save(feedback);

        auditLogService.log("FEEDBACK_SUBMITTED", user, "Feedback", saved.getId(),
                "Rating: " + saved.getRating() + "/5 for complaint " + complaint.getComplaintNumber(), null);

        return FeedbackResponseDto.fromEntity(saved);
    }

    @Transactional
    public FeedbackResponseDto submitFeedback(Long complaintId, FeedbackCreateDto feedbackDto, UserPrincipal currentUser) {
        return submitFeedback(String.valueOf(complaintId), feedbackDto, currentUser);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDto getFeedback(String identifier, UserPrincipal currentUser) {
        Complaint complaint = findComplaintByIdOrNumber(identifier);
        validateAccess(complaint, currentUser);

        return feedbackRepository.findByComplaintId(complaint.getId())
                .map(FeedbackResponseDto::fromEntity)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDto getFeedback(Long complaintId, UserPrincipal currentUser) {
        return getFeedback(String.valueOf(complaintId), currentUser);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponseDto> getEscalatedAndOverdueComplaints(Pageable pageable) {
        Page<Complaint> page = complaintRepository.findEscalatedAndOverdueComplaints(LocalDate.now(), pageable);
        List<ComplaintResponseDto> dtos = page.getContent().stream()
                .map(this::mapToDto)
                .toList();
        return PagedResponse.of(page, dtos);
    }

    private ComplaintResponseDto mapToDto(Complaint c) {
        ComplaintResponseDto dto = ComplaintResponseDto.fromEntity(c);
        if (dto != null) {
            dto.setTransfersCount((int) complaintTransferRepository.countByComplaintId(c.getId()));
            if (c.getStatus() == ComplaintStatus.MERGED) {
                dto.setMerged(true);
                complaintRelationRepository.findActiveMergedRelationBySecondaryId(c.getId()).ifPresent(rel -> {
                    if (rel.getPrimaryComplaint() != null) {
                        dto.setMergedIntoComplaintId(rel.getPrimaryComplaint().getId());
                        dto.setMergedIntoComplaintNumber(rel.getPrimaryComplaint().getComplaintNumber());
                    }
                });
            }
            int mergedCount = complaintRelationRepository.findActiveMergeRelationsByPrimaryId(c.getId()).size();
            dto.setMergedCasesCount(mergedCount);
            int relatedCount = complaintRelationRepository.findActiveRelatedRelationsByComplaintId(c.getId()).size();
            dto.setRelatedCasesCount(relatedCount);
        }
        return dto;
    }

    private void validateAccess(Complaint complaint, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        boolean isOfficer = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTION_OFFICER"));
        if (isOfficer) {
            if (currentUser.getInstitutionId() != null &&
                complaint.getInstitution() != null &&
                currentUser.getInstitutionId().equals(complaint.getInstitution().getId())) {
                return;
            }
            throw new UnauthorizedException("You are not authorized to view complaints for this institution");
        }

        boolean isComplainant = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPLAINANT"));
        if (isComplainant) {
            if (complaint.getComplainant().getId().equals(currentUser.getId())) {
                return;
            }
            throw new UnauthorizedException("You can only view your own complaints");
        }

        throw new UnauthorizedException("Access denied");
    }
}
