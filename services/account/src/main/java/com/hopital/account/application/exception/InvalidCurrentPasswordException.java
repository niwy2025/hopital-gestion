package com.hopital.account.application.exception;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        super("Le mot de passe actuel est incorrect.");
    }
}
