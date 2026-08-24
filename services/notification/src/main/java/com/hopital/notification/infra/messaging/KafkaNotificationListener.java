package com.hopital.notification.infra.messaging;

import com.hopital.notification.application.domain.NotificationRequestEvent;
import com.hopital.notification.application.service.NotificationDispatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationListener {

    private final NotificationDispatchService notificationDispatchService;

    public KafkaNotificationListener(NotificationDispatchService notificationDispatchService) {
        this.notificationDispatchService = notificationDispatchService;
    }

    @KafkaListener(topics = "${hospital.notification.topic}")
    public void consume(NotificationRequestEvent event) {
        notificationDispatchService.dispatch(event);
    }
}
