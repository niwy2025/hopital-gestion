package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.application.domain.PrescriptionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A prescription enriched with the patient and passage context required by the pharmacy. */
public record PharmacyPrescriptionResponse(
        UUID id,
        String code,
        UUID patientId,
        String patientCode,
        String patientFirstName,
        String patientLastName,
        String patientMiddleName,
        UUID passageId,
        String passageCode,
        UUID hospitalId,
        String hospitalCode,
        String serviceName,
        PrescriptionSource source,
        PrescriptionStatus status,
        String externalPrescriberName,
        String externalReference,
        String notes,
        Instant createdAt,
        String createdByUsername,
        List<PrescriptionItemResponse> items,
        List<PrescriptionDispenseResponse> dispenses) {
}
