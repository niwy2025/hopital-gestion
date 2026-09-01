package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Input for a request created from a patient care episode. */
public record CreatePatientPassageAnalysisRequest(
        @NotBlank @Size(max = 30) String laboratoryCode,
        @NotBlank @Size(max = 50) String analysisCode,
        @NotBlank @Size(max = 200) String analysisName) {
}
