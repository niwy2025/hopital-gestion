package com.hopital.laboratory.application.exception;

public class LaboratoryResourceNotFoundException extends RuntimeException {

    public LaboratoryResourceNotFoundException(String resource, String code) {
        super(resource + " avec le code " + code + " est introuvable.");
    }
}
