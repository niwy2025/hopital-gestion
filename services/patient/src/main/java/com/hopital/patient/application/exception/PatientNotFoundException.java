package com.hopital.patient.application.exception;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String code) {
        super("Le patient avec le code " + code + " est introuvable.");
    }
}
