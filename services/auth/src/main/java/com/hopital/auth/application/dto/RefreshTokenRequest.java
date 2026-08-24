package com.hopital.auth.application.dto;

public record RefreshTokenRequest(String refreshToken, String userAgent) {
}
