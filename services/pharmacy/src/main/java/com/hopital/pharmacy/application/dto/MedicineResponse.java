package com.hopital.pharmacy.application.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicineResponse(
        UUID id,
        String code,
        String genericName,
        String commercialName,
        String dosage,
        String pharmaceuticalForm,
        String presentation,
        boolean active,
        Instant createdAt,
        String createdByUsername) {
}
