package com.hopital.organization.application.dto;

import java.util.UUID;

public record HospitalLaboratoryResponse(
        UUID id,
        String code,
        String name,
        String hospitalCode,
        String hospitalName,
        String location,
        String phoneNumber,
        boolean active) {
}
