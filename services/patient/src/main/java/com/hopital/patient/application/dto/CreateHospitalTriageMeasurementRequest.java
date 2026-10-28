package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.TriageMeasurementValueKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateHospitalTriageMeasurementRequest(
        @NotBlank(message = "Le code de la mesure est obligatoire.")
        @Pattern(regexp = "^[A-Z0-9_]{2,40}$", message = "Le code doit contenir 2 à 40 caractères A-Z, 0-9 ou _.")
        String code,
        @NotBlank(message = "Le libellé est obligatoire.")
        @Size(max = 150, message = "Le libellé est trop long.")
        String label,
        @Size(max = 40, message = "L’unité est trop longue.")
        String unit,
        @NotNull(message = "Le type de valeur est obligatoire.") TriageMeasurementValueKind valueKind,
        BigDecimal minValue,
        BigDecimal maxValue,
        Integer displayOrder) {
}
