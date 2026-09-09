package com.hopital.laboratory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateAnalysisResultRequest(
        @NotBlank @Size(max = 30) String analysisRequestCode,
        @Size(max = 1000) String resultValue,
        @Size(max = 100) String unit,
        @Size(max = 255) String referenceRange,
        @Size(max = 1000) String comment,
        @Size(max = 100) List<@NotNull @Valid CreateAnalysisResultValueRequest> values) {

    public CreateAnalysisResultRequest(
            String analysisRequestCode,
            String resultValue,
            String unit,
            String referenceRange,
            String comment) {
        this(analysisRequestCode, resultValue, unit, referenceRange, comment, List.of());
    }
}
