package com.hopital.patient.application.domain;

/** Next operational step decided at the end of a clinical assessment. */
public enum ClinicalOrientation {
    OBSERVATION,
    HOSPITALIZATION,
    REFERRAL,
    LABORATORY,
    PHARMACY,
    FOLLOW_UP,
    DISCHARGE,
    OTHER
}
