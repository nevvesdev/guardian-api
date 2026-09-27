package com.nevvesdev.guardianapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResourceResponse {

    private String id;
    private String name;
    private String description;
    private String type;
    private String ownerId;
    private Boolean isActive;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}