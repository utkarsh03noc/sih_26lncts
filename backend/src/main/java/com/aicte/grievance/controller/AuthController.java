package com.aicte.grievance.controller;

import com.aicte.grievance.dto.ApiResponse;
import com.aicte.grievance.dto.LoginRequest;
import com.aicte.grievance.dto.LoginResponse;
import com.aicte.grievance.dto.RegisterRequest;
import com.aicte.grievance.dto.UserDto;
import com.aicte.grievance.security.UserPrincipal;
import com.aicte.grievance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDto userDto = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", userDto));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        UserDto userDto = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved", userDto));
    }
}
