package com.hopital.patient.application.domain;

/** Identifies whether the order was issued in the platform or transcribed from paper. */
public enum PrescriptionSource {
    MEDICAL,
    EXTERNAL_PAPER
}
