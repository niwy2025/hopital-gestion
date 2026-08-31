package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientAuditEventType;
import java.time.Instant;
import java.util.UUID;

public record PatientAuditEventResponse(
        UUID id,
        PatientAuditEventType type,
        String description,
        String operatorUsername,
        Instant occurredAt) {
}
