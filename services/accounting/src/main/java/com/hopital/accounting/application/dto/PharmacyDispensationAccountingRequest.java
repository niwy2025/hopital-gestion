package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Internal outbox event. All business details are reread from the source services. */
public record PharmacyDispensationAccountingRequest(@NotBlank(message = "Le code de délivrance est obligatoire.") String dispenseCode) { }
