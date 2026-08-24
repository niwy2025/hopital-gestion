package com.hopital.auth.application.dto;

public record LoginRequest(String username, String password, String userAgent) {
}
