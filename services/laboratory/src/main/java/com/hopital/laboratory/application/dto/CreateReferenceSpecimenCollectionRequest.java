package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.SpecimenType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Collection performed at the originating hospital before dispatch to a reference laboratory. */
public record CreateReferenceSpecimenCollectionRequest(
        @NotNull SpecimenType specimenType,
        @NotNull @PastOrPresent Instant collectedAt,
        @Size(max = 1000) String collectionNote) {
}
