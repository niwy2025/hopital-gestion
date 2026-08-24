package com.hopital.notification.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hospital.notification")
public record NotificationProperties(String topic) {
}
