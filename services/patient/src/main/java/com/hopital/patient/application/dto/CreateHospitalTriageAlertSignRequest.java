package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateHospitalTriageAlertSignRequest(
        @NotBlank(message = "Le code du signe d’alerte est obligatoire.")
        @Pattern(regexp = "^[A-Z0-9_]{2,40}$", message = "Le code doit contenir 2 à 40 caractères A-Z, 0-9 ou _.")
        String code,
        @NotBlank(message = "Le libellé est obligatoire.")
        @Size(max = 150, message = "Le libellé est trop long.")
        String label,
        Integer displayOrder) {
}
