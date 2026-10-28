package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.TriageMeasurementValueKind;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HospitalTriageMeasurementResponse(
        UUID id,
        UUID hospitalId,
        String code,
        String label,
        String unit,
        TriageMeasurementValueKind valueKind,
        BigDecimal minValue,
        BigDecimal maxValue,
        int displayOrder,
        boolean active,
        Instant createdAt,
        String createdByUsername,
        Instant updatedAt,
        String updatedByUsername) {
}
