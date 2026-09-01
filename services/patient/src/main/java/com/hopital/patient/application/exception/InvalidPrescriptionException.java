package com.hopital.patient.application.exception;

public class InvalidPrescriptionException extends RuntimeException {

    public InvalidPrescriptionException(String message) {
        super(message);
    }
}
