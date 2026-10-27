package com.aicte.grievance.service;

import com.aicte.grievance.dto.AuditLogDto;
import com.aicte.grievance.dto.InstitutionDto;
import com.aicte.grievance.dto.InstitutionMonitoringDto;
import com.aicte.grievance.dto.PagedResponse;
import com.aicte.grievance.entity.AuditLog;
import com.aicte.grievance.entity.Complaint;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.Institution;
import com.aicte.grievance.entity.User;
import com.aicte.grievance.exception.BadRequestException;
import com.aicte.grievance.exception.ResourceNotFoundException;
import com.aicte.grievance.repository.AuditLogRepository;
import com.aicte.grievance.repository.ComplaintRepository;
import com.aicte.grievance.repository.InstitutionRepository;
import com.aicte.grievance.repository.UserRepository;
import com.aicte.grievance.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    public InstitutionService(InstitutionRepository institutionRepository,
                              ComplaintRepository complaintRepository,
                              UserRepository userRepository,
                              AuditLogRepository auditLogRepository,
                              AuditLogService auditLogService) {
        this.institutionRepository = institutionRepository;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InstitutionDto> getInstitutions(String query, Pageable pageable) {
        Page<Institution> page = institutionRepository.searchInstitutions(query, pageable);
        LocalDate today = LocalDate.now();

        List<InstitutionDto> dtos = page.getContent().stream().map(inst -> {
            InstitutionDto dto = InstitutionDto.fromEntity(inst);
            List<Complaint> complaints = complaintRepository.findByInstitutionId(inst.getId());
            long total = complaints.size();
            long resolved = complaints.stream()
                    .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED)
                    .count();
            long open = total - resolved;
            long overdue = complaints.stream()
                    .filter(c -> c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED &&
                            c.getDueDate() != null && c.getDueDate().isBefore(today))
                    .count();
            long bureauCount = complaints.stream()
                    .map(Complaint::getBureau)
                    .filter(Objects::nonNull)
                    .map(b -> b.getId())
                    .distinct()
                    .count();
            long cellCount = complaints.stream()
                    .map(Complaint::getCell)
                    .filter(Objects::nonNull)
                    .map(c -> c.getId())
                    .distinct()
                    .count();

            dto.setTotalComplaints(total);
            dto.setResolvedComplaints(resolved);
            dto.setOpenComplaints(open);
            dto.setOverdueComplaints(overdue);
            dto.setInvolvedBureausCount((int) bureauCount);
            dto.setInvolvedCellsCount((int) cellCount);
            return dto;
        }).toList();

        return PagedResponse.of(page, dtos);
    }

    @Transactional(readOnly = true)
    public List<InstitutionDto> getActiveInstitutions() {
        return institutionRepository.findByActiveTrue().stream()
                .map(InstitutionDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstitutionDto getInstitutionById(Long id) {
        Institution inst = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", id));
        InstitutionDto dto = InstitutionDto.fromEntity(inst);
        LocalDate today = LocalDate.now();

        List<Complaint> complaints = complaintRepository.findByInstitutionId(inst.getId());
        long total = complaints.size();
        long resolved = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED)
                .count();
        long open = total - resolved;
        long overdue = complaints.stream()
                .filter(c -> c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED &&
                        c.getDueDate() != null && c.getDueDate().isBefore(today))
                .count();
        long bureauCount = complaints.stream()
                .map(Complaint::getBureau)
                .filter(Objects::nonNull)
                .map(b -> b.getId())
                .distinct()
                .count();
        long cellCount = complaints.stream()
                .map(Complaint::getCell)
                .filter(Objects::nonNull)
                .map(c -> c.getId())
                .distinct()
                .count();

        dto.setTotalComplaints(total);
        dto.setResolvedComplaints(resolved);
        dto.setOpenComplaints(open);
        dto.setOverdueComplaints(overdue);
        dto.setInvolvedBureausCount((int) bureauCount);
        dto.setInvolvedCellsCount((int) cellCount);
        return dto;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InstitutionMonitoringDto> getInstitutionMonitoringSummary(String query, Pageable pageable) {
        Page<Institution> page = institutionRepository.searchInstitutions(query, pageable);
        LocalDate today = LocalDate.now();

        List<InstitutionMonitoringDto> dtos = page.getContent().stream().map(inst -> {
            InstitutionMonitoringDto dto = new InstitutionMonitoringDto();
            dto.setInstitutionId(inst.getId());
            dto.setInstitutionName(inst.getName());
            dto.setInstitutionCode(inst.getCode());
            dto.setState(inst.getState() != null ? inst.getState() : "N/A");
            dto.setDistrict(inst.getDistrict() != null ? inst.getDistrict() : "N/A");
            dto.setType(inst.getType());

            List<Complaint> complaints = complaintRepository.findByInstitutionId(inst.getId());
            long total = complaints.size();
            long resolved = complaints.stream()
                    .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED)
                    .count();
            long open = total - resolved;
            long overdue = complaints.stream()
                    .filter(c -> c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED &&
                            c.getDueDate() != null && c.getDueDate().isBefore(today))
                    .count();

            List<String> bureaus = complaints.stream()
                    .map(Complaint::getBureau)
                    .filter(Objects::nonNull)
                    .map(b -> b.getCode() != null ? b.getCode() : b.getName())
                    .distinct()
                    .toList();

            List<String> cells = complaints.stream()
                    .map(Complaint::getCell)
                    .filter(Objects::nonNull)
                    .map(c -> c.getCode() != null ? c.getCode() : c.getName())
                    .distinct()
                    .toList();

            dto.setTotalComplaints(total);
            dto.setOpenComplaints(open);
            dto.setResolvedComplaints(resolved);
            dto.setOverdueComplaints(overdue);
            dto.setInvolvedBureausCount(bureaus.size());
            dto.setInvolvedCellsCount(cells.size());
            dto.setInvolvedBureauNames(bureaus);
            dto.setInvolvedCellNames(cells);

            return dto;
        }).toList();

        return PagedResponse.of(page, dtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getInstitutionAuditLogs(Long institutionId, Pageable pageable) {
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));

        Page<AuditLog> page = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("Institution", institutionId, pageable);
        List<AuditLogDto> dtos = page.getContent().stream()
                .map(AuditLogDto::fromEntity)
                .toList();

        return PagedResponse.of(page, dtos);
    }

    @Transactional
    public InstitutionDto createInstitution(InstitutionDto dto, UserPrincipal currentUser) {
        if (institutionRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Institution code already exists: " + dto.getCode());
        }

        if (dto.getAicteReferenceCode() != null && !dto.getAicteReferenceCode().isBlank()) {
            if (institutionRepository.existsByAicteReferenceCode(dto.getAicteReferenceCode().trim())) {
                throw new BadRequestException("AICTE reference code already exists: " + dto.getAicteReferenceCode());
            }
        }

        Institution institution = new Institution();
        institution.setName(dto.getName());
        institution.setCode(dto.getCode());
        institution.setLocation(dto.getLocation());
        institution.setState(dto.getState());
        institution.setDistrict(dto.getDistrict());
        institution.setAddress(dto.getAddress());
        institution.setAicteReferenceCode(dto.getAicteReferenceCode() != null && !dto.getAicteReferenceCode().isBlank() ? dto.getAicteReferenceCode().trim() : null);
        institution.setType(dto.getType());
        institution.setContactEmail(dto.getContactEmail());
        institution.setContactPhone(dto.getContactPhone());
        institution.setActive(true);

        Institution saved = institutionRepository.save(institution);

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditLogService.log("INSTITUTION_CREATED", user, "Institution", saved.getId(),
                "Created institution: " + saved.getName() + " (" + saved.getCode() + ")", null);

        return InstitutionDto.fromEntity(saved);
    }

    @Transactional
    public InstitutionDto updateInstitution(Long id, InstitutionDto dto, UserPrincipal currentUser) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", id));

        if (!institution.getCode().equalsIgnoreCase(dto.getCode()) && institutionRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Institution code already exists: " + dto.getCode());
        }

        if (dto.getAicteReferenceCode() != null && !dto.getAicteReferenceCode().isBlank()) {
            String newRef = dto.getAicteReferenceCode().trim();
            if (!newRef.equalsIgnoreCase(institution.getAicteReferenceCode()) && institutionRepository.existsByAicteReferenceCode(newRef)) {
                throw new BadRequestException("AICTE reference code already exists: " + newRef);
            }
            institution.setAicteReferenceCode(newRef);
        } else {
            institution.setAicteReferenceCode(null);
        }

        institution.setName(dto.getName());
        institution.setCode(dto.getCode());
        institution.setLocation(dto.getLocation());
        institution.setState(dto.getState());
        institution.setDistrict(dto.getDistrict());
        institution.setAddress(dto.getAddress());
        institution.setType(dto.getType());
        institution.setContactEmail(dto.getContactEmail());
        institution.setContactPhone(dto.getContactPhone());
        institution.setActive(dto.isActive());

        Institution saved = institutionRepository.save(institution);

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        auditLogService.log("INSTITUTION_UPDATED", user, "Institution", saved.getId(),
                "Updated institution: " + saved.getName(), null);

        return InstitutionDto.fromEntity(saved);
    }
}
