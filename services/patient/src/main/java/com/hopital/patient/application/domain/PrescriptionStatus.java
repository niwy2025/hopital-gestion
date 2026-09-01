package com.hopital.patient.application.domain;

/** The dispensing module will move the prescription through these states. */
public enum PrescriptionStatus {
    PENDING_DISPENSING,
    PARTIALLY_DISPENSED,
    DISPENSED,
    CANCELLED
}
