package com.hopital.notification.infra.channel;

import com.hopital.notification.application.domain.NotificationChannel;
import com.hopital.notification.application.domain.NotificationRecipient;
import com.hopital.notification.application.domain.NotificationRequestEvent;
import com.hopital.notification.application.service.NotificationChannelSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingSmsNotificationSender implements NotificationChannelSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingSmsNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationRequestEvent event, NotificationRecipient recipient) {
        LOGGER.info("SMS simulé envoyé : notificationId={}, type={}, destinataire={}",
                event.notificationId(), event.type(), recipient.phoneNumber());
    }
}
