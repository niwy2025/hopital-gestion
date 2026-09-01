package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.ClinicalEntryType;
import com.hopital.patient.application.domain.ClinicalOrientation;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientPassageClinicalEntryResponse(
        UUID id,
        UUID passageId,
        ClinicalEntryType entryType,
        String clinicalFindings,
        String diagnosis,
        String carePlan,
        ClinicalOrientation orientation,
        LocalDate followUpOn,
        Instant recordedAt,
        String recordedByUsername) {
}
