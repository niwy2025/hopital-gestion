package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.SpecimenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateSpecimenRequest(
        @NotBlank @Size(max = 30) String analysisRequestCode,
        @NotNull SpecimenType specimenType,
        @NotNull @PastOrPresent Instant collectedAt) {
}
