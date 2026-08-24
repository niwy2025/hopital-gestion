package com.hopital.notification.infra.channel;

import com.hopital.notification.application.domain.NotificationChannel;
import com.hopital.notification.application.domain.NotificationRecipient;
import com.hopital.notification.application.domain.NotificationRequestEvent;
import com.hopital.notification.application.service.NotificationChannelSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailNotificationSender implements NotificationChannelSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationRequestEvent event, NotificationRecipient recipient) {
        LOGGER.info("E-mail simulé envoyé : notificationId={}, type={}, destinataire={}, sujet={}",
                event.notificationId(), event.type(), recipient.email(), event.subject());
    }
}
