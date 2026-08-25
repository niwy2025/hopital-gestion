package com.hopital.personnel.application.exception;

public class PersonnelNotFoundException extends RuntimeException {

    public PersonnelNotFoundException(String personnelId) {
        super("Le membre du personnel " + personnelId + " est introuvable.");
    }
}
