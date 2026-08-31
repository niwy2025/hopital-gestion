package com.hopital.patient.application.exception;

import java.util.UUID;

public class InvalidRegistrationHospitalException extends RuntimeException {

    public InvalidRegistrationHospitalException(UUID hospitalId) {
        super("L’hôpital sélectionné est introuvable ou inactif : " + hospitalId);
    }
}
