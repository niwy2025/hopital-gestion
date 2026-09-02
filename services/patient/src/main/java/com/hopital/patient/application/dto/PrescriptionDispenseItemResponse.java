package com.hopital.patient.application.dto;

import java.util.UUID;

public record PrescriptionDispenseItemResponse(
        UUID id,
        UUID prescriptionItemId,
        String medicineName,
        String dispensedQuantity) {
}
