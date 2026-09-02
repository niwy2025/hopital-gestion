package com.hopital.laboratory.application.domain;

/** Append-only trace of clinically relevant laboratory workflow transitions. */
public enum AnalysisRequestEventType {
    REQUEST_CREATED,
    SPECIMEN_COLLECTED,
    SPECIMEN_DISPATCHED,
    SPECIMEN_RECEIVED,
    SPECIMEN_REJECTED,
    RESULT_ENTERED,
    RESULT_VALIDATED
}
