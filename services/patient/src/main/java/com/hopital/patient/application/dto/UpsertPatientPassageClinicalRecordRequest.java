package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.ClinicalOrientation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Clinical information is deliberately attached to one concrete hospital passage. */
public record UpsertPatientPassageClinicalRecordRequest(
        @NotBlank(message = "Les constatations cliniques sont obligatoires.")
        @Size(max = 8000, message = "Les constatations cliniques sont trop longues.")
        String clinicalFindings,
        @Size(max = 4000, message = "Le diagnostic est trop long.") String diagnosis,
        @Size(max = 8000, message = "La conduite à tenir est trop longue.") String carePlan,
        @NotNull(message = "L’orientation est obligatoire.") ClinicalOrientation orientation,
        LocalDate followUpOn) {
}
