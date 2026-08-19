package com.hopital.auth.application.dto;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String userId,
        String username,
        String email,
        Set<String> roles,
        Set<String> permissions) {
}
