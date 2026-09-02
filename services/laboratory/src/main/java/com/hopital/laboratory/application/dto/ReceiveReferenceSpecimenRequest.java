package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Physical acceptance at the reference laboratory. */
public record ReceiveReferenceSpecimenRequest(
        @NotNull @PastOrPresent Instant receivedAt,
        @Size(max = 1000) String receptionCondition) {
}
