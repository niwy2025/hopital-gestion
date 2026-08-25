package com.hopital.organization.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProvinceStatusRequest(@NotNull Boolean active) {
}
