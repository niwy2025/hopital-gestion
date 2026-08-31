package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.EmergencyContactRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmergencyContactRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Size(max = 30) String phoneNumber,
        @NotNull EmergencyContactRelationship relationship) {
}
