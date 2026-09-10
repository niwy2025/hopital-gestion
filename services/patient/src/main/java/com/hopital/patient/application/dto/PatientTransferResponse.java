package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientTransferStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientTransferResponse(
        UUID id, String code, UUID patientId, String patientCode, String patientName,
        LocalDate patientDateOfBirth, String patientGender,
        UUID sourcePassageId, String sourcePassageCode, UUID sourceHospitalId, String sourceHospitalCode,
        UUID destinationHospitalId, String destinationHospitalCode, UUID destinationPassageId, String destinationPassageCode,
        String sourceHospitalName, String destinationHospitalName,
        String externalFacilityName, String externalFacilityAddress, String destinationContact,
        String reason, String clinicalSummary, String treatmentAndInstructions, String transportDetails,
        boolean urgent, PatientTransferStatus status,
        Instant requestedAt, String requestedByUsername, Instant dispatchedAt, String dispatchedByUsername,
        Instant resolvedAt, String resolvedByUsername, String resolutionNote,
        boolean canDispatch, boolean canCancel, boolean canReceive) {
}
