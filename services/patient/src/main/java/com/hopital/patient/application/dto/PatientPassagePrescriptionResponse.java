package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.application.domain.PrescriptionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PatientPassagePrescriptionResponse(
        UUID id,
        String code,
        UUID passageId,
        PrescriptionSource source,
        PrescriptionStatus status,
        String externalPrescriberName,
        String externalReference,
        String notes,
        Instant createdAt,
        String createdByUsername,
        List<PrescriptionItemResponse> items) {
}
