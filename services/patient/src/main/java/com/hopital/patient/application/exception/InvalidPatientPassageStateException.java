package com.hopital.patient.application.exception;

public class InvalidPatientPassageStateException extends RuntimeException {

    public InvalidPatientPassageStateException(String message) {
        super(message);
    }
}
