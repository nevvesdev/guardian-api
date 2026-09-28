package com.nevvesdev.guardianapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {

    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String userEmail;
    private String userId;
    private String details;
    private String ipAddress;
    private String status;
    private LocalDateTime createdAt;
}