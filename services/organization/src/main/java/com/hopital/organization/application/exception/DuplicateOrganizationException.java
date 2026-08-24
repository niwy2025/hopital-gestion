package com.hopital.organization.application.exception;

public class DuplicateOrganizationException extends RuntimeException {

    public DuplicateOrganizationException(String resource, String code) {
        super(resource + " avec le code " + code + " existe déjà.");
    }
}
