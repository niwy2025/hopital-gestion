package com.hopital.account.application.exception;

public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String field, String value) {
        super("Un compte utilise déjà " + field + " : " + value);
    }
}
