package com.nevvesdev.guardianapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevvesdev.guardianapi.dto.response.ResourceHistoryResponse;
import com.nevvesdev.guardianapi.entity.Resource;
import com.nevvesdev.guardianapi.entity.ResourceHistory;
import com.nevvesdev.guardianapi.exception.ResourceNotFoundException;
import com.nevvesdev.guardianapi.repository.ResourceHistoryRepository;
import com.nevvesdev.guardianapi.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceHistoryService {

    private final ResourceHistoryRepository resourceHistoryRepository;
    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<ResourceHistoryResponse> findByResourceId(String resourceId) {
        return resourceHistoryRepository.findByResourceIdOrderByCreatedAtDesc(resourceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ResourceHistoryResponse> findByChangedBy(String changedBy) {
        return resourceHistoryRepository.findByChangedByOrderByCreatedAtDesc(changedBy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void saveHistory(Resource previous, Resource current, String changeType) {
        String currentUser = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        try {
            String previousValue = previous != null ? objectMapper.writeValueAsString(previous) : null;
            String newValue = current != null ? objectMapper.writeValueAsString(current) : null;

            ResourceHistory history = ResourceHistory.builder()
                    .resourceId(current != null ? current.getId() : previous.getId())
                    .changedBy(currentUser)
                    .changeType(changeType)
                    .previousValue(previousValue)
                    .newValue(newValue)
                    .resourceVersion(current != null ? current.getVersion() : previous.getVersion())
                    .build();

            resourceHistoryRepository.save(history);
            log.debug("Histórico salvo: {} - {} - {}", changeType, currentUser,
                    current != null ? current.getId() : previous.getId());

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar resource para histórico: {}", e.getMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restore(String resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso", resourceId));

        if (!resource.isDeleted()) {
            throw new com.nevvesdev.guardianapi.exception.BusinessException(
                    "Recurso não está deletado: " + resourceId
            );
        }

        Resource previous = cloneResource(resource);
        resource.restore();
        resource.setIsActive(true);
        resourceRepository.save(resource);

        saveHistory(previous, resource, "RESTORE");
        log.info("Recurso restaurado: {}", resourceId);
    }

    private Resource cloneResource(Resource resource) {
        return Resource.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .ownerId(resource.getOwnerId())
                .isActive(resource.getIsActive())
                .build();
    }

    private ResourceHistoryResponse toResponse(ResourceHistory history) {
        return ResourceHistoryResponse.builder()
                .id(history.getId())
                .resourceId(history.getResourceId())
                .changedBy(history.getChangedBy())
                .changeType(history.getChangeType())
                .previousValue(history.getPreviousValue())
                .newValue(history.getNewValue())
                .resourceVersion(history.getResourceVersion())
                .createdAt(history.getCreatedAt())
                .build();
    }
}