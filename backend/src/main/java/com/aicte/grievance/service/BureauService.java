package com.aicte.grievance.service;

import com.aicte.grievance.dto.BureauDto;
import com.aicte.grievance.dto.CellDto;
import com.aicte.grievance.entity.Bureau;
import com.aicte.grievance.entity.Cell;
import com.aicte.grievance.entity.User;
import com.aicte.grievance.exception.BadRequestException;
import com.aicte.grievance.exception.ResourceNotFoundException;
import com.aicte.grievance.repository.BureauRepository;
import com.aicte.grievance.repository.CellRepository;
import com.aicte.grievance.repository.UserRepository;
import com.aicte.grievance.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BureauService {

    private final BureauRepository bureauRepository;
    private final CellRepository cellRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public BureauService(BureauRepository bureauRepository,
                         CellRepository cellRepository,
                         UserRepository userRepository,
                         AuditLogService auditLogService) {
        this.bureauRepository = bureauRepository;
        this.cellRepository = cellRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<BureauDto> getAllBureaus() {
        return bureauRepository.findAll().stream()
                .map(BureauDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BureauDto> getActiveBureaus() {
        return bureauRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(BureauDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BureauDto getBureauById(Long id) {
        Bureau bureau = bureauRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", id));
        return BureauDto.fromEntity(bureau);
    }

    @Transactional
    public BureauDto createBureau(BureauDto dto, UserPrincipal currentUser) {
        if (bureauRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Bureau with code '" + dto.getCode() + "' already exists");
        }

        Bureau bureau = new Bureau();
        bureau.setCode(dto.getCode().trim().toUpperCase());
        bureau.setName(dto.getName().trim());
        bureau.setDescription(dto.getDescription());
        bureau.setContactEmail(dto.getContactEmail());
        bureau.setActive(dto.isActive());

        Bureau saved = bureauRepository.save(bureau);

        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        auditLogService.log("BUREAU_CREATED", user, "Bureau", saved.getId(),
                "Created Bureau: " + saved.getName() + " (" + saved.getCode() + ")", null);

        return BureauDto.fromEntity(saved);
    }

    @Transactional
    public BureauDto updateBureau(Long id, BureauDto dto, UserPrincipal currentUser) {
        Bureau bureau = bureauRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", id));

        if (!bureau.getCode().equalsIgnoreCase(dto.getCode()) && bureauRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Bureau with code '" + dto.getCode() + "' already exists");
        }

        bureau.setCode(dto.getCode().trim().toUpperCase());
        bureau.setName(dto.getName().trim());
        bureau.setDescription(dto.getDescription());
        bureau.setContactEmail(dto.getContactEmail());
        bureau.setActive(dto.isActive());

        Bureau saved = bureauRepository.save(bureau);

        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        auditLogService.log("BUREAU_UPDATED", user, "Bureau", saved.getId(),
                "Updated Bureau: " + saved.getName() + " (" + saved.getCode() + ")", null);

        return BureauDto.fromEntity(saved);
    }

    @Transactional
    public void deleteBureau(Long id, UserPrincipal currentUser) {
        Bureau bureau = bureauRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", id));
        bureau.setActive(false);
        bureauRepository.save(bureau);

        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        auditLogService.log("BUREAU_DEACTIVATED", user, "Bureau", id,
                "Deactivated Bureau: " + bureau.getName(), null);
    }

    // Cell Operations
    @Transactional(readOnly = true)
    public List<CellDto> getAllCells() {
        return cellRepository.findAll().stream()
                .map(CellDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CellDto> getActiveCells() {
        return cellRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(CellDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CellDto> getCellsByBureauId(Long bureauId) {
        return cellRepository.findByBureauIdAndActiveTrue(bureauId).stream()
                .map(CellDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CellDto> getCellsByBureau(Long bureauId) {
        return getCellsByBureauId(bureauId);
    }

    @Transactional(readOnly = true)
    public CellDto getCellById(Long id) {
        Cell cell = cellRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cell", "id", id));
        return CellDto.fromEntity(cell);
    }

    @Transactional
    public CellDto createCell(CellDto dto, UserPrincipal currentUser) {
        if (cellRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Cell with code '" + dto.getCode() + "' already exists");
        }

        Cell cell = new Cell();
        cell.setCode(dto.getCode().trim().toUpperCase());
        cell.setName(dto.getName().trim());
        cell.setDescription(dto.getDescription());
        cell.setContactEmail(dto.getContactEmail());
        cell.setActive(dto.isActive());

        if (dto.getBureauId() != null) {
            Bureau bureau = bureauRepository.findById(dto.getBureauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", dto.getBureauId()));
            cell.setBureau(bureau);
        }

        Cell saved = cellRepository.save(cell);

        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        auditLogService.log("CELL_CREATED", user, "Cell", saved.getId(),
                "Created Cell: " + saved.getName() + " (" + saved.getCode() + ")", null);

        return CellDto.fromEntity(saved);
    }

    @Transactional
    public CellDto updateCell(Long id, CellDto dto, UserPrincipal currentUser) {
        Cell cell = cellRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cell", "id", id));

        if (!cell.getCode().equalsIgnoreCase(dto.getCode()) && cellRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Cell with code '" + dto.getCode() + "' already exists");
        }

        cell.setCode(dto.getCode().trim().toUpperCase());
        cell.setName(dto.getName().trim());
        cell.setDescription(dto.getDescription());
        cell.setContactEmail(dto.getContactEmail());
        cell.setActive(dto.isActive());

        if (dto.getBureauId() != null) {
            Bureau bureau = bureauRepository.findById(dto.getBureauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bureau", "id", dto.getBureauId()));
            cell.setBureau(bureau);
        } else {
            cell.setBureau(null);
        }

        Cell saved = cellRepository.save(cell);

        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        auditLogService.log("CELL_UPDATED", user, "Cell", saved.getId(),
                "Updated Cell: " + saved.getName() + " (" + saved.getCode() + ")", null);

        return CellDto.fromEntity(saved);
    }

    @Transactional
    public void deleteCell(Long id, UserPrincipal currentUser) {
        Cell cell = cellRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cell", "id", id));
        cell.setActive(false);
        cellRepository.save(cell);

        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        auditLogService.log("CELL_DEACTIVATED", user, "Cell", id,
                "Deactivated Cell: " + cell.getName(), null);
    }
}
