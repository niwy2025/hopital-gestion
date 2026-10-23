package com.hopital.patient.application.exception;

public class InvalidPatientCoverageException extends RuntimeException {

    public InvalidPatientCoverageException(String message) {
        super(message);
    }
}
