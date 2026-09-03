package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseAccountingEntryRequest(@NotBlank @Size(max = 1000) String reason) { }
