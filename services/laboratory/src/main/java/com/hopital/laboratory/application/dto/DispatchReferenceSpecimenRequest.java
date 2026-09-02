package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Handover of a collected specimen to the transport chain. */
public record DispatchReferenceSpecimenRequest(
        @NotNull @PastOrPresent Instant dispatchedAt,
        @Size(max = 200) String carrierName,
        @Size(max = 1000) String dispatchNote) {
}
