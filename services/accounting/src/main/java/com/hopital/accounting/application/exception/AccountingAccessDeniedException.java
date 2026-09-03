package com.hopital.accounting.application.exception;

public class AccountingAccessDeniedException extends RuntimeException {
    public AccountingAccessDeniedException() { super("Votre périmètre ne permet pas d'accéder à cette comptabilité hospitalière."); }
}
