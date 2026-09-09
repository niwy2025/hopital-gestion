package com.hopital.laboratory.application.dto;

import java.time.Instant;
import java.util.UUID;

public record DiseaseResponse(
        UUID id,
        String code,
        String name,
        String description,
        String icdSystem,
        String icdCode,
        String icdUri,
        boolean active,
        Instant createdAt) {
}
