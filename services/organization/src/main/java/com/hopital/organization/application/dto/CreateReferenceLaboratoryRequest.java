package com.hopital.organization.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReferenceLaboratoryRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 20) String provinceCode,
        @Size(max = 255) String address,
        @Size(max = 30) String phoneNumber) {
}
