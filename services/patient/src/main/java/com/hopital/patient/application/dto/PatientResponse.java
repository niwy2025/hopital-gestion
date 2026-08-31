package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.Gender;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String code,
        String firstName,
        String lastName,
        String middleName,
        LocalDate dateOfBirth,
        Gender gender,
        String phoneNumber,
        String email,
        String address,
        String nationalIdentifier,
        List<EmergencyContactResponse> emergencyContacts,
        UUID registrationHospitalId,
        String registrationHospitalCode,
        boolean active,
        Instant createdAt,
        String createdByUsername,
        Instant updatedAt,
        String updatedByUsername,
        List<PatientAuditEventResponse> auditEvents) {
}
