package com.hopital.accounting.application.domain;

/** Posted accounting entries are append-only. A correction is a reversal entry. */
public enum AccountingEntryStatus { DRAFT, POSTED, REVERSED }
