package com.hopital.notification.application.service;

import com.hopital.notification.application.domain.NotificationChannel;
import com.hopital.notification.application.domain.NotificationRecipient;
import com.hopital.notification.application.domain.NotificationRequestEvent;
import com.hopital.notification.application.dto.BroadcastAcceptedResponse;
import com.hopital.notification.application.dto.BroadcastRequest;
import com.hopital.notification.application.exception.InvalidBroadcastException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BroadcastApplicationService {

    private final NotificationRequestPublisher notificationRequestPublisher;

    public BroadcastApplicationService(NotificationRequestPublisher notificationRequestPublisher) {
        this.notificationRequestPublisher = notificationRequestPublisher;
    }

    public BroadcastAcceptedResponse queue(BroadcastRequest request) {
        validateRecipients(request);

        Instant queuedAt = Instant.now();
        UUID notificationId = UUID.randomUUID();
        NotificationRequestEvent event = new NotificationRequestEvent(
                notificationId,
                "notification-service",
                request.type(),
                request.channels(),
                request.recipients().stream()
                        .map(recipient -> new NotificationRecipient(
                                recipient.email(), recipient.phoneNumber(), recipient.displayName()))
                        .toList(),
                request.subject(),
                request.body(),
                request.metadata(),
                queuedAt);
        notificationRequestPublisher.publish(event);
        return new BroadcastAcceptedResponse(notificationId, "QUEUED", queuedAt);
    }

    private void validateRecipients(BroadcastRequest request) {
        for (NotificationChannel channel : request.channels()) {
            boolean hasRecipient = request.recipients().stream().anyMatch(recipient -> supports(channel, recipient));
            if (!hasRecipient) {
                throw new InvalidBroadcastException("Aucun destinataire compatible avec le canal " + channel + ".");
            }
        }
    }

    private boolean supports(NotificationChannel channel, com.hopital.notification.application.dto.BroadcastRecipientRequest recipient) {
        return switch (channel) {
            case EMAIL -> recipient.email() != null && !recipient.email().isBlank();
            case SMS -> recipient.phoneNumber() != null && !recipient.phoneNumber().isBlank();
        };
    }
}
