package com.hopital.patient.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Paper prescription recorded directly at the pharmacy.
 *
 * <p>The application creates its pharmacy passage itself so the caller never
 * has to manufacture a patient visit before recording a sale.</p>
 */
public record CreatePharmacyExternalPrescriptionRequest(
        @NotNull(message = "Le patient est obligatoire.") UUID patientId,
        UUID hospitalId,
        @Size(max = 200, message = "Le nom du prescripteur externe est trop long.") String externalPrescriberName,
        @Size(max = 150, message = "La référence externe est trop longue.") String externalReference,
        @Size(max = 4000, message = "La note est trop longue.") String notes,
        @NotEmpty(message = "Ajoutez au moins un médicament.")
        @Size(max = 30, message = "Une ordonnance ne peut pas contenir plus de 30 médicaments.")
        List<@Valid PrescriptionItemRequest> items) {
}
