package com.hopital.organization.application.exception;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(String resource, String code) {
        super(resource + " avec le code " + code + " est introuvable.");
    }
}
