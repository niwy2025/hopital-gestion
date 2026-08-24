package com.hopital.notification.infra.messaging;

import com.hopital.notification.application.config.NotificationProperties;
import com.hopital.notification.application.domain.NotificationRequestEvent;
import com.hopital.notification.application.service.NotificationRequestPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationRequestPublisher implements NotificationRequestPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaNotificationRequestPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationProperties properties;

    public KafkaNotificationRequestPublisher(KafkaTemplate<String, Object> kafkaTemplate, NotificationProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(NotificationRequestEvent event) {
        kafkaTemplate.send(properties.topic(), event.notificationId().toString(), event)
                .whenComplete((result, error) -> {
                    if (error == null) {
                        LOGGER.info("Notification {} mise en file depuis {}.", event.notificationId(), event.sourceService());
                    } else {
                        LOGGER.error("Impossible de mettre la notification {} en file.", event.notificationId(), error);
                    }
                });
    }
}
