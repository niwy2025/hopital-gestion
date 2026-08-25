package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ValidateAnalysisResultRequest(@NotBlank @Size(max = 100) String validatedBy) {
}
