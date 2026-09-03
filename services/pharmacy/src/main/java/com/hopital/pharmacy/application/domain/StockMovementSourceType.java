package com.hopital.pharmacy.application.domain;

/**
 * Business document that originated an immutable stock movement.
 *
 * <p>The value is intentionally separate from {@link StockMovementType}: one
 * movement can be a dispensing while still being linked to a particular
 * prescription delivery. This gives the future accounting service a stable,
 * idempotent business reference instead of having to parse a free-text note.</p>
 */
public enum StockMovementSourceType {
    PRESCRIPTION_DISPENSE
}
