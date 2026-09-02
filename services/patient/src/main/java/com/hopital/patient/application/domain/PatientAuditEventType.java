package com.hopital.patient.application.domain;

public enum PatientAuditEventType {
    CREATED,
    UPDATED,
    STATUS_CHANGED,
    CLINICAL_ENTRY_ADDED,
    PRESCRIPTION_ADDED,
    PRESCRIPTION_DISPENSED,
    DOCUMENT_ADDED,
    DOCUMENT_REMOVED,
    IMPORTED
}
