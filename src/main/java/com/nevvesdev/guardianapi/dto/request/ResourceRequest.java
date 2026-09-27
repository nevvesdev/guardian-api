package com.nevvesdev.guardianapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private String description;

    @NotBlank(message = "Tipo é obrigatório")
    private String type;
}