package com.hopital.account.application.exception;

public class InvalidHospitalAssignmentException extends RuntimeException {

    public InvalidHospitalAssignmentException(String hospitalId) {
        super("La référence d’hôpital est invalide : " + hospitalId);
    }
}
