package com.hopital.patient.application.domain;

/**
 * Delivery state of a pharmacy dispense sent to the accounting service.
 *
 * <p>The source delivery remains immutable and successful independently of
 * this state. A {@link #PENDING} event is retried until the accounting service
 * acknowledges the document it created.</p>
 */
public enum AccountingOutboxStatus {
    PENDING,
    POSTED
}
