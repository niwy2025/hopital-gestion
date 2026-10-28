package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record TriageCustomMeasurementValueRequest(
        @NotNull(message = "La mesure personnalisée est obligatoire.") UUID measurementId,
        BigDecimal numericValue,
        @Size(max = 500, message = "La valeur texte est trop longue.") String textValue) {
}
