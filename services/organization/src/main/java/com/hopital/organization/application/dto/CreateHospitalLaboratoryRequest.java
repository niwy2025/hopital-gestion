package com.hopital.organization.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHospitalLaboratoryRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 30) String hospitalCode,
        @Size(max = 255) String location,
        @Size(max = 30) String phoneNumber) {
}
