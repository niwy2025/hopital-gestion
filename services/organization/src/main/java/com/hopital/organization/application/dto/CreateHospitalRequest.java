package com.hopital.organization.application.dto;

import com.hopital.organization.application.domain.HospitalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHospitalRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String name,
        @NotNull HospitalType type,
        @NotBlank @Size(max = 30) String healthZoneCode,
        @Size(max = 30) String healthAreaCode,
        @Size(max = 255) String address,
        @Size(max = 30) String phoneNumber) {
}
