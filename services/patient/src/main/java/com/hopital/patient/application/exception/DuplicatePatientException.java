package com.hopital.patient.application.exception;

public class DuplicatePatientException extends RuntimeException {

    public DuplicatePatientException(String code) {
        super("Un patient possède déjà le code " + code + ".");
    }
}
