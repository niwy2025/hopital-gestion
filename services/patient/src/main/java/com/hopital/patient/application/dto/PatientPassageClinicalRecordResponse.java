package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.ClinicalOrientation;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientPassageClinicalRecordResponse(
        UUID id,
        UUID passageId,
        String clinicalFindings,
        String diagnosis,
        String carePlan,
        ClinicalOrientation orientation,
        LocalDate followUpOn,
        Instant createdAt,
        String createdByUsername,
        Instant updatedAt,
        String updatedByUsername) {
}
