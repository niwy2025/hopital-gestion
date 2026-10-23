package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PassageResponsibilityStepKind;
import java.time.Instant;
import java.util.UUID;

public record PassageResponsibilityStepResponse(
        UUID id,
        UUID passageId,
        PassageResponsibilityStepKind stepKind,
        String stepLabel,
        UUID relatedEntityId,
        UUID responsiblePersonnelId,
        String responsiblePersonnelEmployeeNumber,
        String responsiblePersonnelName,
        String responsiblePersonnelJobTitle,
        Instant assignedAt,
        String assignedByUsername) {
}
