package com.hopital.pharmacy.application.domain;

/** Delivery state of an immutable stock-out event to the accounting service. */
public enum StockMovementAccountingOutboxStatus {
    PENDING,
    POSTED,
    EXCLUDED
}
