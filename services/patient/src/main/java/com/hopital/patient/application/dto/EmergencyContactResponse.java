package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.EmergencyContactRelationship;
import java.util.UUID;

public record EmergencyContactResponse(
        UUID id,
        String fullName,
        String phoneNumber,
        EmergencyContactRelationship relationship) {
}
