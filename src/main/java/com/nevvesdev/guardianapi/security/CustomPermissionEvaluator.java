package com.nevvesdev.guardianapi.security;

import com.nevvesdev.guardianapi.entity.Resource;
import com.nevvesdev.guardianapi.repository.ResourceRepository;
import com.nevvesdev.guardianapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (targetDomainObject instanceof Resource resource) {
            return evaluateResourcePermission(authentication, resource, permission.toString());
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if ("Resource".equals(targetType)) {
            Optional<Resource> resource = resourceRepository.findByIdAndDeletedAtIsNull(targetId.toString());
            return resource.map(r -> evaluateResourcePermission(authentication, r, permission.toString()))
                    .orElse(false);
        }

        return false;
    }

    private boolean evaluateResourcePermission(Authentication authentication,
                                               Resource resource,
                                               String permission) {
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            log.debug("Acesso ADMIN concedido para {} no recurso {}", email, resource.getId());
            return true;
        }

        boolean isOwner = userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(user -> user.getId().equals(resource.getOwnerId()))
                .orElse(false);

        return switch (permission.toUpperCase()) {
            case "READ" -> isOwner || hasRolePermission(authentication, "RESOURCE_READ");
            case "WRITE" -> isOwner || hasRolePermission(authentication, "RESOURCE_WRITE");
            case "DELETE" -> isOwner || hasRolePermission(authentication, "RESOURCE_DELETE");
            default -> {
                log.warn("Permissão desconhecida: {}", permission);
                yield false;
            }
        };
    }

    private boolean hasRolePermission(Authentication authentication, String permissionName) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permissionName));
    }
}