package com.hopital.notification.application.dto;

import java.time.Instant;
import java.util.UUID;

public record BroadcastAcceptedResponse(UUID notificationId, String status, Instant queuedAt) {
}
