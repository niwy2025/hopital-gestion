package com.hopital.auth.application.dto;

import java.time.Instant;

public record KnownDeviceResponse(
        String userAgent,
        Instant lastSeenAt,
        long signInCount,
        boolean currentDevice) {
}
