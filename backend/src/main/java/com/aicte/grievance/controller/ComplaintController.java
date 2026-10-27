package com.aicte.grievance.controller;

import com.aicte.grievance.dto.*;
import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.Priority;
import com.aicte.grievance.security.UserPrincipal;
import com.aicte.grievance.service.ComplaintClassificationService;
import com.aicte.grievance.service.ComplaintMergeService;
import com.aicte.grievance.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintClassificationService complaintClassificationService;
    private final ComplaintMergeService complaintMergeService;

    public ComplaintController(ComplaintService complaintService,
                               ComplaintClassificationService complaintClassificationService,
                               ComplaintMergeService complaintMergeService) {
        this.complaintService = complaintService;
        this.complaintClassificationService = complaintClassificationService;
        this.complaintMergeService = complaintMergeService;
    }

    @PostMapping("/merge")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<UnifiedCaseDto>> mergeComplaints(
            @Valid @RequestBody ComplaintMergeRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UnifiedCaseDto response = complaintMergeService.mergeComplaints(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaints successfully merged into Unified Case File", response));
    }

    @PostMapping("/unmerge")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<UnifiedCaseDto>> unmergeComplaints(
            @Valid @RequestBody ComplaintUnmergeRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UnifiedCaseDto response = complaintMergeService.unmergeComplaints(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint successfully unmerged and restored", response));
    }

    @PostMapping("/link-related")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<CaseRelationDto>> linkAsRelated(
            @Valid @RequestBody CaseLinkRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CaseRelationDto response = complaintMergeService.linkAsRelated(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaints successfully linked as related", response));
    }

    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<CaseCompareDto>> compareComplaints(
            @RequestParam(value = "caseAId", required = false) String caseAId,
            @RequestParam(value = "caseBId", required = false) String caseBId,
            @RequestParam(value = "caseA", required = false) String caseA,
            @RequestParam(value = "caseB", required = false) String caseB,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String idA = (caseAId != null && !caseAId.isBlank()) ? caseAId : caseA;
        String idB = (caseBId != null && !caseBId.isBlank()) ? caseBId : caseB;
        if (idA == null || idB == null) {
            throw new com.aicte.grievance.exception.BadRequestException("Both caseA and caseB identifiers (caseAId and caseBId) are required.");
        }
        CaseCompareDto response = complaintMergeService.compareComplaints(idA, idB, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint comparison generated", response));
    }

    @GetMapping("/{id}/unified-file")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER', 'COMPLAINANT')")
    public ResponseEntity<ApiResponse<UnifiedCaseDto>> getUnifiedCaseFile(@PathVariable String id) {
        UnifiedCaseDto response = complaintMergeService.getUnifiedCaseFile(id);
        return ResponseEntity.ok(ApiResponse.success("Unified case file retrieved", response));
    }

    @GetMapping("/{id}/merge-candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<List<DuplicateMatchDto>>> getMergeCandidates(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<DuplicateMatchDto> response = complaintMergeService.getMergeCandidates(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Merge candidate cases retrieved", response));
    }

    @PostMapping("/classify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ComplaintClassificationResponseDto>> classifyComplaint(
            @Valid @RequestBody ComplaintClassificationRequestDto request) {
        ComplaintClassificationResponseDto response = complaintClassificationService.classifyComplaint(request);
        return ResponseEntity.ok(ApiResponse.success("Complaint analyzed successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> createComplaint(
            @Valid @RequestBody ComplaintCreateDto createDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponseDto response = complaintService.createComplaint(createDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint registered successfully", response));
    }

    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<List<DuplicateMatchDto>>> checkDuplicates(
            @Valid @RequestBody DuplicateCheckRequestDto request) {
        List<DuplicateMatchDto> matches = complaintService.checkDuplicates(request);
        return ResponseEntity.ok(ApiResponse.success("Duplicate check completed", matches));
    }

    @GetMapping("/duplicates")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponseDto>>> getPotentialDuplicates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ComplaintResponseDto> duplicates = complaintService.getPotentialDuplicates(pageable);
        return ResponseEntity.ok(ApiResponse.success("Potential duplicate complaints retrieved", duplicates));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponseDto>>> getComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long bureauId,
            @RequestParam(required = false) Long cellId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ComplaintResponseDto> complaints = complaintService.getFilteredComplaints(
                status, category, priority, institutionId, bureauId, cellId, search, currentUser, pageable);

        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", complaints));
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<ApiResponse<List<ComplaintResponseDto>>> getComplaintsByInstitution(
            @PathVariable Long institutionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ComplaintResponseDto> complaints = complaintService.getComplaintsByInstitution(institutionId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Institution complaints retrieved", complaints));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> getComplaintById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponseDto response = complaintService.getComplaintById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved", response));
    }

    @GetMapping("/public/track/{complaintNumber}")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> trackPublicComplaint(
            @PathVariable String complaintNumber) {
        ComplaintResponseDto response = complaintService.getComplaintByNumber(complaintNumber);
        return ResponseEntity.ok(ApiResponse.success("Complaint status found", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> updateComplaintStatus(
            @PathVariable String id,
            @Valid @RequestBody ComplaintStatusUpdateDto updateDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponseDto response = complaintService.updateComplaintStatus(id, updateDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> assignComplaint(
            @PathVariable String id,
            @Valid @RequestBody ComplaintAssignDto assignDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponseDto response = complaintService.assignComplaint(id, assignDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned successfully", response));
    }

    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> transferComplaint(
            @PathVariable String id,
            @Valid @RequestBody ComplaintTransferRequestDto transferDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponseDto response = complaintService.transferComplaint(id, transferDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint transferred successfully across Bureau/Cell", response));
    }

    @GetMapping("/{id}/transfers")
    public ResponseEntity<ApiResponse<List<ComplaintTransferDto>>> getComplaintTransfers(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ComplaintTransferDto> transfers = complaintService.getComplaintTransfers(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint transfers retrieved", transfers));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> reopenComplaint(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String remarks = body != null ? body.get("remarks") : null;
        ComplaintResponseDto response = complaintService.reopenComplaint(id, remarks, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint reopened successfully", response));
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<ApiResponse<List<ComplaintUpdateDto>>> getComplaintUpdates(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ComplaintUpdateDto> updates = complaintService.getComplaintUpdates(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint updates retrieved", updates));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentDto>>> getComplaintAttachments(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AttachmentDto> attachments = complaintService.getComplaintAttachments(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint attachments retrieved", attachments));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('COMPLAINANT')")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> submitFeedback(
            @PathVariable String id,
            @Valid @RequestBody FeedbackCreateDto feedbackDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FeedbackResponseDto response = complaintService.submitFeedback(id, feedbackDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully. Thank you!", response));
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> getFeedback(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FeedbackResponseDto response = complaintService.getFeedback(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved", response));
    }

    @GetMapping("/escalations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponseDto>>> getEscalations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ComplaintResponseDto> escalations = complaintService.getEscalatedAndOverdueComplaints(pageable);
        return ResponseEntity.ok(ApiResponse.success("Escalations and overdue complaints retrieved", escalations));
    }
}
