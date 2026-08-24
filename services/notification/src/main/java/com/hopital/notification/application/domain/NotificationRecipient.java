package com.hopital.notification.application.domain;

public record NotificationRecipient(String email, String phoneNumber, String displayName) {

    public boolean supports(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> email != null && !email.isBlank();
            case SMS -> phoneNumber != null && !phoneNumber.isBlank();
        };
    }
}
