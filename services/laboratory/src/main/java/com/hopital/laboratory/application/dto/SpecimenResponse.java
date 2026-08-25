package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.SpecimenStatus;
import com.hopital.laboratory.application.domain.SpecimenType;
import java.time.Instant;
import java.util.UUID;

public record SpecimenResponse(
        UUID id,
        String code,
        String analysisRequestCode,
        String patientName,
        SpecimenType specimenType,
        SpecimenStatus status,
        Instant collectedAt,
        Instant receivedAt) {
}
