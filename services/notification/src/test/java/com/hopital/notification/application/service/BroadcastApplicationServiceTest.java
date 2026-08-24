package com.hopital.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.hopital.notification.application.domain.NotificationChannel;
import com.hopital.notification.application.domain.NotificationRequestEvent;
import com.hopital.notification.application.dto.BroadcastRecipientRequest;
import com.hopital.notification.application.dto.BroadcastRequest;
import com.hopital.notification.application.exception.InvalidBroadcastException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BroadcastApplicationServiceTest {

    @Mock
    private NotificationRequestPublisher notificationRequestPublisher;

    @Captor
    private ArgumentCaptor<NotificationRequestEvent> eventCaptor;

    @InjectMocks
    private BroadcastApplicationService broadcastApplicationService;

    @Test
    void queuesAnEmailAndSmsBroadcast() {
        var response = broadcastApplicationService.queue(new BroadcastRequest(
                "APPOINTMENT_REMINDER",
                List.of(NotificationChannel.EMAIL, NotificationChannel.SMS),
                List.of(new BroadcastRecipientRequest("patient@hopital.local", "+243810000000", "Patient")),
                "Rappel de rendez-vous",
                "Votre rendez-vous est prévu demain.",
                Map.of("appointmentId", "apt-1")));

        verify(notificationRequestPublisher).publish(eventCaptor.capture());
        assertThat(response.status()).isEqualTo("QUEUED");
        assertThat(eventCaptor.getValue().sourceService()).isEqualTo("notification-service");
        assertThat(eventCaptor.getValue().channels()).containsExactly(NotificationChannel.EMAIL, NotificationChannel.SMS);
    }

    @Test
    void rejectsAnEmailBroadcastWithoutEmailRecipient() {
        var request = new BroadcastRequest(
                "APPOINTMENT_REMINDER",
                List.of(NotificationChannel.EMAIL),
                List.of(new BroadcastRecipientRequest(null, "+243810000000", "Patient")),
                "Rappel",
                "Votre rendez-vous est prévu demain.",
                Map.of());

        assertThatThrownBy(() -> broadcastApplicationService.queue(request))
                .isInstanceOf(InvalidBroadcastException.class)
                .hasMessageContaining("EMAIL");
    }
}
