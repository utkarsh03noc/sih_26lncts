package com.aicte.grievance.service;

import com.aicte.grievance.dto.UserDto;
import com.aicte.grievance.entity.Role;
import com.aicte.grievance.entity.User;
import com.aicte.grievance.exception.ResourceNotFoundException;
import com.aicte.grievance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllOfficers() {
        return userRepository.findByRole(Role.INSTITUTION_OFFICER).stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getOfficersByInstitution(Long institutionId) {
        return userRepository.findByInstitutionIdAndRole(institutionId, Role.INSTITUTION_OFFICER).stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDto.fromEntity(user);
    }
}
