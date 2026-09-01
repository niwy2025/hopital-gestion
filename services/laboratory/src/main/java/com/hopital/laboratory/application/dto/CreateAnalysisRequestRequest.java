package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.LaboratoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAnalysisRequestRequest(
        @NotNull LaboratoryType laboratoryType,
        @NotBlank @Size(max = 30) String laboratoryCode,
        @NotBlank @Size(max = 100) String patientReference,
        @NotBlank @Size(max = 200) String patientName,
        @NotBlank @Size(max = 200) String analysisName,
        @Size(max = 200) String requesterName) {
}
