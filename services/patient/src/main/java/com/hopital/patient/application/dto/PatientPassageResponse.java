package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import java.time.Instant;
import java.util.UUID;

public record PatientPassageResponse(
        UUID id,
        String code,
        UUID hospitalId,
        String hospitalCode,
        PatientPassageType type,
        String serviceName,
        String reason,
        PatientPassageStatus status,
        Instant arrivedAt,
        Instant closedAt,
        String createdByUsername,
        String closedByUsername) {
}
