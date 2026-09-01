package com.hopital.patient.application.exception;

public class InvalidPatientDocumentException extends RuntimeException {

    public InvalidPatientDocumentException(String message) {
        super(message);
    }
}
