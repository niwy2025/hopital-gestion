package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignPatientPassageResponsiblePersonnelRequest(
        @NotNull(message = "Le personnel responsable est obligatoire.") UUID personnelId) {
}
