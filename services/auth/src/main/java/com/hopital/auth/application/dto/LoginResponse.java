package com.hopital.auth.application.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Instant expiresAt,
        long refreshExpiresIn,
        Instant refreshExpiresAt,
        String userAgent) {
}
