package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Civil identity used to detect a potential existing patient before registration. */
public record PatientDuplicateCheckRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 100) String middleName,
        @NotNull @PastOrPresent LocalDate dateOfBirth,
        @NotNull Gender gender) {
}
