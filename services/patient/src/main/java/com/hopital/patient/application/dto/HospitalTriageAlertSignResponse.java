package com.hopital.patient.application.dto;

import java.time.Instant;
import java.util.UUID;

public record HospitalTriageAlertSignResponse(
        UUID id,
        UUID hospitalId,
        String code,
        String label,
        int displayOrder,
        boolean active,
        Instant createdAt,
        String createdByUsername,
        Instant updatedAt,
        String updatedByUsername) {
}
