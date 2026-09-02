package com.hopital.pharmacy.application.exception;

public class PharmacyResourceNotFoundException extends RuntimeException {
    public PharmacyResourceNotFoundException(String resource) { super(resource + " est introuvable."); }
}
