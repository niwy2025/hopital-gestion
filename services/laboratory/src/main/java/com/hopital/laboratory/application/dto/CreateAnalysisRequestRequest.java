package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnalysisRequestRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 30) String referenceLaboratoryCode,
        @NotBlank @Size(max = 100) String patientReference,
        @NotBlank @Size(max = 200) String patientName,
        @NotBlank @Size(max = 50) String analysisCode,
        @NotBlank @Size(max = 200) String analysisName,
        @Size(max = 200) String requesterName) {
}
