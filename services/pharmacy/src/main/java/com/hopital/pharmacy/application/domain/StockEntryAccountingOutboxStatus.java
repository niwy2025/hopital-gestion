package com.hopital.pharmacy.application.domain;

/**
 * Delivery state of an immutable stock receipt sent to the accounting service.
 *
 * <p>The pharmacy receipt is already valid independently of this state. A
 * pending event is retried in the background until the accounting voucher is
 * acknowledged.</p>
 */
public enum StockEntryAccountingOutboxStatus {
    PENDING,
    POSTED
}
