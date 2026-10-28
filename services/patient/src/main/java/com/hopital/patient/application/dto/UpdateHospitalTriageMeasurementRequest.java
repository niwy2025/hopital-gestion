package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.TriageMeasurementValueKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateHospitalTriageMeasurementRequest(
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
