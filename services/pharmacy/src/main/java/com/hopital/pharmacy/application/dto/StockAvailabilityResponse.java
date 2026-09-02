package com.hopital.pharmacy.application.dto;

import java.util.UUID;

/** Response used by the entry form before a medicine is added to stock. */
public record StockAvailabilityResponse(UUID medicineId, boolean exists, StockBalanceResponse stock) { }
