package com.nevvesdev.guardianapi.service;

import com.nevvesdev.guardianapi.dto.request.ResourceRequest;
import com.nevvesdev.guardianapi.dto.response.ResourceResponse;
import com.nevvesdev.guardianapi.entity.Resource;
import com.nevvesdev.guardianapi.entity.User;
import com.nevvesdev.guardianapi.exception.BusinessException;
import com.nevvesdev.guardianapi.exception.ResourceNotFoundException;
import com.nevvesdev.guardianapi.repository.ResourceRepository;
import com.nevvesdev.guardianapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResourceResponse create(ResourceRequest request) {
        User currentUser = getCurrentUser();

        Resource resource = Resource.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .ownerId(currentUser.getId())
                .isActive(true)
                .build();

        Resource saved = resourceRepository.save(resource);

        log.info("Recurso criado: {} por usuário: {}", saved.getId(), currentUser.getEmail());

        return toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<ResourceResponse> findAll() {
        return resourceRepository.findAllActive()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasPermission(#id, 'Resource', 'READ')")
    public ResourceResponse findById(String id) {
        Resource resource = resourceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso", id));

        return toResponse(resource);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'Resource', 'WRITE')")
    public ResourceResponse update(String id, ResourceRequest request) {
        Resource resource = resourceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso", id));

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());

        Resource updated = resourceRepository.save(resource);

        log.info("Recurso atualizado: {} por usuário: {}",
                id, SecurityContextHolder.getContext().getAuthentication().getName());

        return toResponse(updated);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'Resource', 'DELETE')")
    public void delete(String id) {
        Resource resource = resourceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso", id));

        resource.setDeletedAt(LocalDateTime.now());
        resource.setIsActive(false);
        resourceRepository.save(resource);

        log.info("Recurso deletado (soft delete): {} por usuário: {}",
                id, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ResourceResponse> findByOwner(String ownerId) {
        return resourceRepository.findByOwnerIdAndDeletedAtIsNull(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
    }

    private ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .ownerId(resource.getOwnerId())
                .isActive(resource.getIsActive())
                .version(resource.getVersion())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}