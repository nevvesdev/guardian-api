package com.nevvesdev.guardianapi.service;

import com.nevvesdev.guardianapi.dto.request.AssignRoleRequest;
import com.nevvesdev.guardianapi.dto.response.UserResponse;
import com.nevvesdev.guardianapi.entity.Role;
import com.nevvesdev.guardianapi.entity.User;
import com.nevvesdev.guardianapi.exception.BusinessException;
import com.nevvesdev.guardianapi.exception.ResourceNotFoundException;
import com.nevvesdev.guardianapi.repository.RoleRepository;
import com.nevvesdev.guardianapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return toResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse assignRole(String userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        Role role = roleRepository.findByName(request.getRoleName().toUpperCase())
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(request.getRoleName().toUpperCase())
                            .description("Role " + request.getRoleName())
                            .build();
                    return roleRepository.save(newRole);
                });

        boolean alreadyHasRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(role.getName()));

        if (alreadyHasRole) {
            throw new BusinessException("Usuário já possui a role: " + role.getName());
        }

        user.getRoles().add(role);
        userRepository.save(user);

        log.info("Role {} atribuída ao usuário {}", role.getName(), user.getEmail());

        return toResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse removeRole(String userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleName));

        boolean removed = user.getRoles().removeIf(r -> r.getName().equals(role.getName()));

        if (!removed) {
            throw new BusinessException("Usuário não possui a role: " + roleName);
        }

        userRepository.save(user);

        log.info("Role {} removida do usuário {}", roleName, user.getEmail());

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}