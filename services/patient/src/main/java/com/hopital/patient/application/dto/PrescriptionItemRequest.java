package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PrescriptionItemRequest(
        UUID medicineId,
        @NotBlank(message = "Le médicament est obligatoire.")
        @Size(max = 250, message = "Le nom du médicament est trop long.")
        String medicineName,
        @Size(max = 150, message = "Le dosage est trop long.") String dosage,
        @Size(max = 100, message = "La voie d'administration est trop longue.") String administrationRoute,
        @Size(max = 150, message = "La fréquence est trop longue.") String frequency,
        @Size(max = 150, message = "La durée est trop longue.") String duration,
        @Size(max = 100, message = "La quantité est trop longue.") String quantity,
        @Size(max = 4000, message = "Les instructions sont trop longues.") String instructions) {
}
