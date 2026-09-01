package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import java.time.Instant;
import java.util.UUID;

/**
 * Passage exposed by the provincial registry. It intentionally contains only
 * the patient identity needed to locate the dossier, not the full profile.
 */
public record PatientPassageSummaryResponse(
        UUID id,
        String code,
        UUID patientId,
        String patientCode,
        String patientFirstName,
        String patientLastName,
        String patientMiddleName,
        UUID hospitalId,
        String hospitalCode,
        PatientPassageType type,
        String serviceName,
        String reason,
        PatientPassageStatus status,
        Instant arrivedAt,
        Instant closedAt,
        String createdByUsername,
        String closedByUsername,
        UUID responsiblePersonnelId,
        String responsiblePersonnelEmployeeNumber,
        String responsiblePersonnelName,
        String responsiblePersonnelJobTitle,
        Instant responsibleAssignedAt,
        String responsibleAssignedByUsername) {
}
