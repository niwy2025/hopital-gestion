package com.hopital.patient.application.domain;

/** Events emitted by accounting for one pharmacy dispense invoice. */
public enum PharmacyDispenseAccountingEventType {
    INVOICE_ISSUED,
    PAYMENT_RECORDED
}
