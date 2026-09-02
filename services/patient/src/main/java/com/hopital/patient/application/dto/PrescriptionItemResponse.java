package com.hopital.patient.application.dto;

import java.util.UUID;

public record PrescriptionItemResponse(
        UUID id,
        UUID medicineId,
        String medicineName,
        String dosage,
        String administrationRoute,
        String frequency,
        String duration,
        String quantity,
        String instructions,
        int displayOrder) {
}
