package com.hopital.laboratory.application.exception;

public class DuplicateLaboratoryResourceException extends RuntimeException {

    public DuplicateLaboratoryResourceException(String resource, String code) {
        super(resource + " avec le code " + code + " existe déjà.");
    }
}
