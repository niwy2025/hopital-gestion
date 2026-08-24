package com.hopital.notification.application.dto;

import com.hopital.notification.application.domain.NotificationChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record BroadcastRequest(
        @NotBlank String type,
        @NotEmpty List<NotificationChannel> channels,
        @NotEmpty List<@Valid BroadcastRecipientRequest> recipients,
        @NotBlank String subject,
        @NotBlank String body,
        Map<String, String> metadata) {
}
