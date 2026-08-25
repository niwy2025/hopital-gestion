package com.hopital.personnel.application.exception;

public class InvalidPersonnelReferenceException extends RuntimeException {

    public InvalidPersonnelReferenceException(String message) {
        super(message);
    }
}
