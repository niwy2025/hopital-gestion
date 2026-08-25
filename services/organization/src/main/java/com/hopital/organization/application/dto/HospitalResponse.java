package com.hopital.organization.application.dto;

import com.hopital.organization.application.domain.HospitalType;
import java.util.UUID;

public record HospitalResponse(
        UUID id,
        String code,
        String name,
        HospitalType type,
        String provinceCode,
        String healthZoneCode,
        String healthAreaCode,
        String address,
        String phoneNumber,
        boolean active) {
}
