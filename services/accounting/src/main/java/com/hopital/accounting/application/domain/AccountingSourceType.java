package com.hopital.accounting.application.domain;

/** Stable source key used to make integrations idempotent. */
public enum AccountingSourceType {
    MANUAL_ENTRY,
    MANUAL_INVOICE,
    PHARMACY_DISPENSE,
    PHARMACY_STOCK_RECEIPT,
    PHARMACY_STOCK_MOVEMENT,
    OPENING_BALANCE
}
