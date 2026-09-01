package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientPassageStatus;
import java.util.UUID;

/**
 * Minimal internal snapshot used by the laboratory service when it creates or
 * processes an analysis for one patient passage. It is deliberately excluded
 * from the public gateway routes.
 */
public record PatientPassageLaboratoryReferenceResponse(
        UUID passageId,
        String passageCode,
        UUID patientId,
        String patientCode,
        String patientName,
        UUID hospitalId,
        String hospitalCode,
        String serviceName,
        PatientPassageStatus status) {
}
