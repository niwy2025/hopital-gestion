package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnalysisResultRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 30) String analysisRequestCode,
        @NotBlank @Size(max = 1000) String resultValue,
        @Size(max = 100) String unit,
        @Size(max = 255) String referenceRange,
        @Size(max = 1000) String comment) {
}
