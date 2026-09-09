package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateClinicalInterpretationRequest(
        @NotBlank @Size(max = 2000) String clinicalConclusion,
        @NotEmpty @Size(max = 20) Set<UUID> diseaseIds) {
}
