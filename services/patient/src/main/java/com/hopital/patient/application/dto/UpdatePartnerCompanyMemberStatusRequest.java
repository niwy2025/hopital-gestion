package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePartnerCompanyMemberStatusRequest(@NotNull Boolean active) {
}
