package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.BureauDto;
import com.aicte.grievance.dto.CellDto;
import com.aicte.grievance.security.UserPrincipal;
import com.aicte.grievance.service.BureauService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BureauController {

    private final BureauService bureauService;

    public BureauController(BureauService bureauService) {
        this.bureauService = bureauService;
    }

    // --- Bureau Endpoints ---

    @GetMapping("/bureaus")
    public ResponseEntity<ApiResponse<List<BureauDto>>> getAllBureaus() {
        List<BureauDto> list = bureauService.getAllBureaus();
        return ResponseEntity.ok(ApiResponse.success("Bureaus retrieved", list));
    }

    @GetMapping("/bureaus/active")
    public ResponseEntity<ApiResponse<List<BureauDto>>> getActiveBureaus() {
        List<BureauDto> list = bureauService.getActiveBureaus();
        return ResponseEntity.ok(ApiResponse.success("Active bureaus retrieved", list));
    }

    @GetMapping("/bureaus/{id}")
    public ResponseEntity<ApiResponse<BureauDto>> getBureauById(@PathVariable Long id) {
        BureauDto dto = bureauService.getBureauById(id);
        return ResponseEntity.ok(ApiResponse.success("Bureau retrieved", dto));
    }

    @PostMapping("/bureaus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BureauDto>> createBureau(
            @Valid @RequestBody BureauDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BureauDto created = bureauService.createBureau(dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Bureau created successfully", created));
    }

    @PutMapping("/bureaus/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BureauDto>> updateBureau(
            @PathVariable Long id,
            @Valid @RequestBody BureauDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BureauDto updated = bureauService.updateBureau(id, dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Bureau updated successfully", updated));
    }

    @DeleteMapping("/bureaus/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBureau(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        bureauService.deleteBureau(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Bureau deactivated/deleted successfully", null));
    }

    // --- Cell Endpoints ---

    @GetMapping("/cells")
    public ResponseEntity<ApiResponse<List<CellDto>>> getAllCells() {
        List<CellDto> list = bureauService.getAllCells();
        return ResponseEntity.ok(ApiResponse.success("Cells retrieved", list));
    }

    @GetMapping("/cells/active")
    public ResponseEntity<ApiResponse<List<CellDto>>> getActiveCells() {
        List<CellDto> list = bureauService.getActiveCells();
        return ResponseEntity.ok(ApiResponse.success("Active cells retrieved", list));
    }

    @GetMapping("/cells/bureau/{bureauId}")
    public ResponseEntity<ApiResponse<List<CellDto>>> getCellsByBureau(@PathVariable Long bureauId) {
        List<CellDto> list = bureauService.getCellsByBureau(bureauId);
        return ResponseEntity.ok(ApiResponse.success("Cells for bureau retrieved", list));
    }

    @GetMapping("/cells/{id}")
    public ResponseEntity<ApiResponse<CellDto>> getCellById(@PathVariable Long id) {
        CellDto dto = bureauService.getCellById(id);
        return ResponseEntity.ok(ApiResponse.success("Cell retrieved", dto));
    }

    @PostMapping("/cells")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CellDto>> createCell(
            @Valid @RequestBody CellDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CellDto created = bureauService.createCell(dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Cell created successfully", created));
    }

    @PutMapping("/cells/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CellDto>> updateCell(
            @PathVariable Long id,
            @Valid @RequestBody CellDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CellDto updated = bureauService.updateCell(id, dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Cell updated successfully", updated));
    }

    @DeleteMapping("/cells/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCell(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        bureauService.deleteCell(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Cell deactivated/deleted successfully", null));
    }
}
