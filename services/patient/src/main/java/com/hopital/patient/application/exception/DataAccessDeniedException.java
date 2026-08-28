package com.hopital.patient.application.exception;

public class DataAccessDeniedException extends RuntimeException {

    public DataAccessDeniedException() {
        super("Votre affectation ne permet pas d'accéder à ces données.");
    }
}
