package com.hopital.pharmacy.application.domain;

/** State reserved for the future accounting service; pharmacy never changes a posted entry. */
public enum AccountingPostingStatus {
    PENDING_ACCOUNTING,
    POSTED
}
