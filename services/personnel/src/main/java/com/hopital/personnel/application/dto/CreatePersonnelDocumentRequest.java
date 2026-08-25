package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.PersonnelDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePersonnelDocumentRequest(
        @NotNull(message = "Le type de document est obligatoire.")
        PersonnelDocumentType documentType,
        @NotBlank(message = "Le nom du fichier est obligatoire.")
        @Size(max = 255, message = "Le nom du fichier ne peut pas dépasser 255 caractères.")
        String fileName,
        @NotBlank(message = "Le format du fichier est obligatoire.")
        @Size(max = 120, message = "Le format du fichier est invalide.")
        String contentType,
        @NotBlank(message = "Le contenu du fichier est obligatoire.")
        @Size(max = 2_800_000, message = "Le fichier ne peut pas dépasser 2 Mo.")
        String contentBase64) {
}
