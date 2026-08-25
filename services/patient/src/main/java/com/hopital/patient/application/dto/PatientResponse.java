package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.Gender;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String code,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        String phoneNumber,
        String address,
        String registrationHospitalCode,
        boolean active,
        Instant createdAt) {
}
