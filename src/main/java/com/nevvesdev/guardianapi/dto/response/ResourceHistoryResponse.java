package com.nevvesdev.guardianapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResourceHistoryResponse {

    private String id;
    private String resourceId;
    private String changedBy;
    private String changeType;
    private String previousValue;
    private String newValue;
    private Long resourceVersion;
    private LocalDateTime createdAt;
}