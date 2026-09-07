package com.hopital.patient.application.domain;

/**
 * Priority selected by the clinician according to the local triage protocol.
 * This value is intentionally not inferred automatically from vital signs.
 */
public enum TriagePriority {
    RED,
    YELLOW,
    GREEN
}
