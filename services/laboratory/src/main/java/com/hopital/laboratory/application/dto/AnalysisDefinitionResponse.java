package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.SpecimenType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisDefinitionResponse(
        UUID id,
        String code,
        String name,
        String description,
        SpecimenType specimenType,
        boolean active,
        List<AnalysisParameterResponse> parameters,
        Instant createdAt) {
}
