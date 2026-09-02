package com.hopital.patient.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePrescriptionDispenseRequest(
        boolean complete,
        @Size(max = 4000, message = "La note de délivrance est trop longue.") String notes,
        @NotEmpty(message = "Sélectionnez au moins un médicament délivré.")
        @Size(max = 30, message = "Une délivrance ne peut pas contenir plus de 30 médicaments.")
        List<@Valid PrescriptionDispenseItemRequest> items) {
}
