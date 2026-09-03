package com.hopital.patient.application.exception;

public class PrescriptionDispenseNotFoundException extends RuntimeException {
    public PrescriptionDispenseNotFoundException(String dispenseCode) {
        super("La délivrance " + dispenseCode + " est introuvable.");
    }
}
