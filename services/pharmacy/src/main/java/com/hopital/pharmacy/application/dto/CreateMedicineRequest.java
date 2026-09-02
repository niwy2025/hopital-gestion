package com.hopital.pharmacy.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMedicineRequest(
        @NotBlank(message = "La dénomination commune est obligatoire.")
        @Size(max = 200) String genericName,
        @Size(max = 200) String commercialName,
        @Size(max = 100) String dosage,
        @Size(max = 100) String pharmaceuticalForm,
        @Size(max = 150) String presentation) {
}
