package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisValueType;
import com.hopital.laboratory.application.domain.SpecimenType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateAnalysisDefinitionRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull SpecimenType specimenType,
        @NotEmpty @Size(max = 100) List<@Valid Parameter> parameters) {

    public record Parameter(
            @NotBlank @Size(max = 200) String name,
            @NotNull AnalysisValueType valueType,
            @Size(max = 100) String unit,
            @Size(max = 255) String referenceRange,
            @Size(max = 20) List<@NotBlank @Size(max = 100) String> qualitativeOptions,
            boolean required) {
    }
}
