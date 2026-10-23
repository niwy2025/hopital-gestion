package com.hopital.patient.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreatePartnerCompanyRequest(
        @NotNull UUID hospitalId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 30) String phoneNumber,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String address) {
}
