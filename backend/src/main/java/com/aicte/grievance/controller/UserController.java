package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.UserDto;
import com.aicte.grievance.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/officers")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllOfficers() {
        List<UserDto> officers = userService.getAllOfficers();
        return ResponseEntity.ok(ApiResponse.success("Officers retrieved", officers));
    }

    @GetMapping("/officers/institution/{institutionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION_OFFICER')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getOfficersByInstitution(@PathVariable Long institutionId) {
        List<UserDto> officers = userService.getOfficersByInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success("Institution officers retrieved", officers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }
}
