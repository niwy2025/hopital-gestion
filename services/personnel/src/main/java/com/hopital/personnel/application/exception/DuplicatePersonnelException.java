package com.hopital.personnel.application.exception;

public class DuplicatePersonnelException extends RuntimeException {

    public DuplicatePersonnelException(String message) {
        super(message);
    }
}
