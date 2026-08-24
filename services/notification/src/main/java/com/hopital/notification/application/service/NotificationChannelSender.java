package com.hopital.notification.application.service;

import com.hopital.notification.application.domain.NotificationChannel;
import com.hopital.notification.application.domain.NotificationRecipient;
import com.hopital.notification.application.domain.NotificationRequestEvent;

public interface NotificationChannelSender {

    NotificationChannel channel();

    void send(NotificationRequestEvent event, NotificationRecipient recipient);
}
