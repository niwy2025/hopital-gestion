package com.hopital.pharmacy.application.exception;

public class InvalidStockEntryException extends RuntimeException {
    public InvalidStockEntryException(String message) { super(message); }
}
