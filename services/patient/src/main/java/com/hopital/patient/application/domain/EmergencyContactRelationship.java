package com.hopital.patient.application.domain;

/** Relationship between a patient and an emergency contact. */
public enum EmergencyContactRelationship {
    PARENT,
    SPOUSE,
    CHILD,
    SIBLING,
    GUARDIAN,
    RELATIVE,
    FRIEND,
    COLLEAGUE,
    OTHER
}
