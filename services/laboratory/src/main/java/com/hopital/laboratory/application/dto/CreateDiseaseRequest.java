package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDiseaseRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Size(max = 30) String icdSystem,
        @Size(max = 50) String icdCode,
        @Size(max = 500) String icdUri) {
}
