package com.nevvesdev.guardianapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotBlank(message = "Role é obrigatória")
    private String roleName;
}