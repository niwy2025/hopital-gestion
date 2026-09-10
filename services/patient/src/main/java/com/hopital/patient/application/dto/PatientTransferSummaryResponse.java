package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientTransferStatus;
import java.time.Instant;
import java.util.UUID;

public record PatientTransferSummaryResponse(
        UUID id, String code, String patientCode, String patientName,
        String sourceHospitalCode, String sourceHospitalName,
        String destinationHospitalCode, String destinationHospitalName, String externalFacilityName,
        boolean urgent, PatientTransferStatus status, Instant requestedAt) {
}
