package com.hopital.patient.application.domain;

/**
 * Patient-side copy of the accounting invoice status.  It deliberately uses a
 * local enum so that the patient service does not take a Java dependency on
 * the accounting service.
 */
public enum PharmacyDispenseAccountingInvoiceStatus {
    DRAFT,
    ISSUED,
    PARTIALLY_PAID,
    PAID,
    CANCELLED
}
