package com.nevvesdev.guardianapi.security;

import com.nevvesdev.guardianapi.entity.AuditLog;
import com.nevvesdev.guardianapi.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(com.nevvesdev.guardianapi.security.AuditAction)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditAction auditAction = method.getAnnotation(AuditAction.class);

        String userEmail = "anonymous";
        String userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            userEmail = authentication.getName();
        }

        String ipAddress = getIpAddress();
        String entityId = extractEntityId(joinPoint.getArgs());
        String status = "SUCCESS";
        String details = null;

        try {
            Object result = joinPoint.proceed();
            details = "Operação realizada com sucesso";
            return result;
        } catch (Throwable ex) {
            status = "FAILURE";
            details = "Erro: " + ex.getMessage();
            throw ex;
        } finally {
            try {
                AuditLog auditLog = AuditLog.builder()
                        .action(auditAction.action())
                        .entityType(auditAction.entityType())
                        .entityId(entityId)
                        .userEmail(userEmail)
                        .userId(userId)
                        .details(details)
                        .ipAddress(ipAddress)
                        .status(status)
                        .build();

                auditLogRepository.save(auditLog);
                log.debug("Audit log salvo: {} - {} - {}", auditAction.action(), userEmail, status);
            } catch (Exception e) {
                log.error("Erro ao salvar audit log: {}", e.getMessage());
            }
        }
    }

    private String extractEntityId(Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String str && str.length() == 36) {
                    return str;
                }
            }
        }
        return null;
    }

    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Não foi possível obter IP: {}", e.getMessage());
        }
        return "unknown";
    }
}