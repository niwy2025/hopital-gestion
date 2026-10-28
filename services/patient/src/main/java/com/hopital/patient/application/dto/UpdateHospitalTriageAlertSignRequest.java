package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHospitalTriageAlertSignRequest(
        @NotBlank(message = "Le libellé est obligatoire.")
        @Size(max = 150, message = "Le libellé est trop long.")
        String label,
        Integer displayOrder) {
}
