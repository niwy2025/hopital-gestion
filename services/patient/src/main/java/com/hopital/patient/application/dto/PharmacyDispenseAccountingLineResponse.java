package com.hopital.patient.application.dto;

import java.util.UUID;

/** One prescribed item actually handed to the patient during a dispense. */
public record PharmacyDispenseAccountingLineResponse(
        UUID prescriptionItemId,
        UUID medicineId,
        String medicineName,
        String dosage,
        String dispensedQuantity) {
}
