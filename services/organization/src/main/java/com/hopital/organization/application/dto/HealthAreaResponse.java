package com.hopital.organization.application.dto;

public record HealthAreaResponse(
        String code, String name, String provinceCode, String healthZoneCode, boolean active) {
}
