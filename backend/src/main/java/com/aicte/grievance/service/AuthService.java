package com.aicte.grievance.service;

import com.aicte.grievance.dto.LoginRequest;
import com.aicte.grievance.dto.LoginResponse;
import com.aicte.grievance.dto.RegisterRequest;
import com.aicte.grievance.dto.UserDto;
import com.aicte.grievance.entity.Institution;
import com.aicte.grievance.entity.Role;
import com.aicte.grievance.entity.User;
import com.aicte.grievance.exception.BadRequestException;
import com.aicte.grievance.repository.InstitutionRepository;
import com.aicte.grievance.repository.UserRepository;
import com.aicte.grievance.security.JwtTokenProvider;
import com.aicte.grievance.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       InstitutionRepository institutionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        auditLogService.log("USER_LOGIN", user, "User", user.getId(), "User logged in: " + user.getUsername(), null);

        Long instId = user.getInstitution() != null ? user.getInstitution().getId() : null;
        String instName = user.getInstitution() != null ? user.getInstitution().getName() : null;

        return new LoginResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                instId,
                instName
        );
    }

    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        Institution institution = null;
        if (registerRequest.getInstitutionId() != null) {
            institution = institutionRepository.findById(registerRequest.getInstitutionId())
                    .orElse(null);
        }

        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.COMPLAINANT;

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFullName(),
                registerRequest.getPhone(),
                role,
                institution
        );

        User savedUser = userRepository.save(user);

        auditLogService.log("USER_REGISTERED", savedUser, "User", savedUser.getId(),
                "Registered new user: " + savedUser.getUsername() + " with role " + savedUser.getRole(), null);

        return UserDto.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        return UserDto.fromEntity(user);
    }
}
