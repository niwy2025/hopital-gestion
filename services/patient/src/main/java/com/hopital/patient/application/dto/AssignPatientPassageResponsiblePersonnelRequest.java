package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PassageResponsibilityStepKind;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AssignPatientPassageResponsiblePersonnelRequest(
        @NotNull(message = "Le personnel responsable est obligatoire.") UUID personnelId,
        PassageResponsibilityStepKind stepKind,
        @Size(max = 150, message = "Le libellé de l’étape est trop long.") String stepLabel) {
}
