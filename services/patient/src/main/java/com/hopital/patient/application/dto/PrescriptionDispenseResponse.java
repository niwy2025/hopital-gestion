package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PrescriptionDispenseCompletion;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PrescriptionDispenseResponse(
        UUID id,
        String code,
        PrescriptionDispenseCompletion completion,
        String notes,
        Instant dispensedAt,
        String dispensedByUsername,
        List<PrescriptionDispenseItemResponse> items) {
}
