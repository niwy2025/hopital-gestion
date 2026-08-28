package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.PersonnelAssignmentScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePersonnelAssignmentRequest(
        @NotNull(message = "Le périmètre d'affectation est obligatoire.") PersonnelAssignmentScope scope,
        @Size(max = 36, message = "La référence de l'hôpital est invalide.") String hospitalId,
        @Size(max = 150) String departmentName,
        @Size(max = 150) String unitName,
        @NotBlank(message = "La fonction exercée est obligatoire.") @Size(max = 150) String positionTitle,
        @NotNull(message = "La date de début est obligatoire.") LocalDate startsOn,
        boolean primaryAssignment,
        @Size(max = 1000) String notes) {
}
