package com.hopital.notification.application.dto;

public record BroadcastRecipientRequest(String email, String phoneNumber, String displayName) {
}
