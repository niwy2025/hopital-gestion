package com.hopital.patient.application.exception;

public class DuplicatePatientException extends RuntimeException {

    public DuplicatePatientException(String duplicateCriterion) {
        super("Un dossier patient existe déjà pour " + duplicateCriterion + ".");
    }
}
