package com.hopital.account.application.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String role) {
        super("Role not found: " + role);
    }
}
