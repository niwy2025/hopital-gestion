package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.Gender;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Deliberately limited payload for registers and patient selectors. */
public record PatientSummaryResponse(
        UUID id,
        String code,
        String firstName,
        String lastName,
        String middleName,
        LocalDate dateOfBirth,
        Gender gender,
        String phoneNumber,
        UUID registrationHospitalId,
        String registrationHospitalCode,
        boolean active,
        Instant createdAt) {
}
