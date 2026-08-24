package com.hopital.notification.application.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrat Kafka partagé par les services producteurs de notifications.
 * Les producteurs publient ce JSON dans hospital.notification.request.v1.
 */
public record NotificationRequestEvent(
        UUID notificationId,
        String sourceService,
        String type,
        List<NotificationChannel> channels,
        List<NotificationRecipient> recipients,
        String subject,
        String body,
        Map<String, String> metadata,
        Instant requestedAt) {

    public NotificationRequestEvent {
        notificationId = notificationId == null ? UUID.randomUUID() : notificationId;
        channels = channels == null ? List.of() : List.copyOf(channels);
        recipients = recipients == null ? List.of() : List.copyOf(recipients);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        requestedAt = requestedAt == null ? Instant.now() : requestedAt;
    }
}
