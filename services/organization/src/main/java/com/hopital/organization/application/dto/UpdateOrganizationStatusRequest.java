package com.hopital.organization.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateOrganizationStatusRequest(@NotNull Boolean active) {
}
