package com.hopital.notification.application.service;

import com.hopital.notification.application.domain.NotificationRequestEvent;

public interface NotificationRequestPublisher {

    void publish(NotificationRequestEvent event);
}
