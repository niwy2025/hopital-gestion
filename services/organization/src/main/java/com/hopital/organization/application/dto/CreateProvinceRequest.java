package com.hopital.organization.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProvinceRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 150) String name) {
}
