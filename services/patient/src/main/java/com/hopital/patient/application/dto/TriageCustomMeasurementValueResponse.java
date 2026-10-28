package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.TriageMeasurementValueKind;
import java.math.BigDecimal;
import java.util.UUID;

public record TriageCustomMeasurementValueResponse(
        UUID measurementId,
        String code,
        String label,
        String unit,
        TriageMeasurementValueKind valueKind,
        BigDecimal numericValue,
        String textValue) {
}
