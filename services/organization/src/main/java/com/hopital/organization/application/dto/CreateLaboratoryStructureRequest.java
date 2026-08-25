package com.hopital.organization.application.dto;

import com.hopital.organization.application.domain.LaboratoryStructureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLaboratoryStructureRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String name,
        @NotNull LaboratoryStructureType type,
        @NotBlank @Size(max = 30) String referenceLaboratoryCode) {
}
