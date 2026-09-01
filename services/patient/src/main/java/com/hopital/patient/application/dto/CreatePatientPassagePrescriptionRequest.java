package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PrescriptionSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** A prescription is always attached to one patient passage. */
public record CreatePatientPassagePrescriptionRequest(
        @NotNull(message = "L'origine de l'ordonnance est obligatoire.") PrescriptionSource source,
        @Size(max = 200, message = "Le nom du prescripteur externe est trop long.") String externalPrescriberName,
        @Size(max = 150, message = "La référence externe est trop longue.") String externalReference,
        @Size(max = 4000, message = "La note est trop longue.") String notes,
        @NotEmpty(message = "Ajoutez au moins un médicament.")
        @Size(max = 30, message = "Une ordonnance ne peut pas contenir plus de 30 médicaments.")
        List<@Valid PrescriptionItemRequest> items) {
}
