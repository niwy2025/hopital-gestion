package com.hopital.patient.application.domain;

public enum PatientAuditEventType {
    CREATED,
    UPDATED,
    STATUS_CHANGED,
    DOCUMENT_ADDED,
    DOCUMENT_REMOVED,
    IMPORTED
}
