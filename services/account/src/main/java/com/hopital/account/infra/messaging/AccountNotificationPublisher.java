package com.hopital.account.infra.messaging;

import com.hopital.account.application.domain.AccountCreatedEvent;
import com.hopital.account.application.dto.AccountResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AccountNotificationPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountNotificationPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String notificationTopic;

    public AccountNotificationPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${hospital.notification.topic:hospital.notification.request.v1}") String notificationTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationTopic = notificationTopic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAccountCreated(AccountCreatedEvent event) {
        AccountResponse account = event.account();
        NotificationRequest request = new NotificationRequest(
                UUID.randomUUID(),
                "account-service",
                "ACCOUNT_CREATED",
                List.of("EMAIL"),
                List.of(new NotificationRecipient(account.email(), null, account.displayName())),
                "Bienvenue sur Hopital Gestion",
                "Bonjour " + account.displayName() + ", votre compte a été créé.",
                Map.of("accountId", account.id()),
                Instant.now());

        kafkaTemplate.send(notificationTopic, account.id(), request)
                .whenComplete((result, error) -> {
                    if (error == null) {
                        LOGGER.info("Notification de création du compte {} mise en file.", account.id());
                    } else {
                        LOGGER.error("Impossible de mettre en file la notification du compte {}.", account.id(), error);
                    }
                });
    }

    private record NotificationRequest(
            UUID notificationId,
            String sourceService,
            String type,
            List<String> channels,
            List<NotificationRecipient> recipients,
            String subject,
            String body,
            Map<String, String> metadata,
            Instant requestedAt) {
    }

    private record NotificationRecipient(String email, String phoneNumber, String displayName) {
    }
}
