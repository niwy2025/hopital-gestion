package com.hopital.patient.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePartnerCompanyRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String legalName,
        @Size(max = 30) String phoneNumber,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String address) {
}
