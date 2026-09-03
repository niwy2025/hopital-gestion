package com.hopital.accounting.application.exception;

public class AccountingResourceNotFoundException extends RuntimeException {
    public AccountingResourceNotFoundException(String resource) { super(resource + " est introuvable."); }
}
