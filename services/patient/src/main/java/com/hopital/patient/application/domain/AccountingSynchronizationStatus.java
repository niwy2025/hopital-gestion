package com.hopital.patient.application.domain;

/**
 * State of the asynchronous accounting projection attached to a pharmacy
 * dispense.  The actual accounting document remains authoritative.
 */
public enum AccountingSynchronizationStatus {
    NOT_REQUESTED,
    PENDING,
    SYNCHRONIZED
}
