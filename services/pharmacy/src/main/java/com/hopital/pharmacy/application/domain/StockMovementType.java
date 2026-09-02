package com.hopital.pharmacy.application.domain;

/** Every quantity variation is immutable and traceable in the stock ledger. */
public enum StockMovementType {
    ENTRY,
    DISPENSING,
    TRANSFER_OUT,
    LOSS,
    EXPIRY
}
