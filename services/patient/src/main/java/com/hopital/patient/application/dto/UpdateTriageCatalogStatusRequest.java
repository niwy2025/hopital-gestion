package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTriageCatalogStatusRequest(
        @NotNull(message = "Le statut actif est obligatoire.") Boolean active) {
}
