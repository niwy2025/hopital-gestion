package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 100) String middleName,
        @NotNull @PastOrPresent LocalDate dateOfBirth,
        @NotNull Gender gender,
        @Size(max = 30) String phoneNumber,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String address,
        @Size(max = 100) String nationalIdentifier,
        @Size(max = 200) String emergencyContactName,
        @Size(max = 30) String emergencyContactPhone,
        @Size(max = 100) String emergencyContactRelationship,
        @NotNull UUID registrationHospitalId) {
}
