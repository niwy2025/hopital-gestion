package com.hopital.auth.application.exception;

public class AuthException extends RuntimeException {

    private final AuthFailureCode code;

    public AuthException(String message) {
        this(AuthFailureCode.AUTHENTICATION_FAILED, message);
    }

    public AuthException(AuthFailureCode code, String message) {
        super(message);
        this.code = code;
    }

    public AuthFailureCode getCode() {
        return code;
    }
}
