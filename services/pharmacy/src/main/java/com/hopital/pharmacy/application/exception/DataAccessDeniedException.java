package com.hopital.pharmacy.application.exception;

public class DataAccessDeniedException extends RuntimeException {
    public DataAccessDeniedException() { super("Votre périmètre ne permet pas cette opération de pharmacie."); }
}
