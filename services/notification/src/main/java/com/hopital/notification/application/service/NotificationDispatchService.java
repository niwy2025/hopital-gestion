package com.hopital.notification.application.service;

import com.hopital.notification.application.domain.NotificationChannel;
import com.hopital.notification.application.domain.NotificationRequestEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {

    private final Map<NotificationChannel, NotificationChannelSender> senders;

    public NotificationDispatchService(List<NotificationChannelSender> senders) {
        Map<NotificationChannel, NotificationChannelSender> byChannel = new EnumMap<>(NotificationChannel.class);
        senders.forEach(sender -> byChannel.put(sender.channel(), sender));
        this.senders = Map.copyOf(byChannel);
    }

    public void dispatch(NotificationRequestEvent event) {
        event.channels().stream().distinct().forEach(channel -> {
            NotificationChannelSender sender = senders.get(channel);
            if (sender == null) {
                throw new IllegalStateException("Aucun émetteur configuré pour le canal " + channel + ".");
            }
            event.recipients().stream()
                    .filter(recipient -> recipient.supports(channel))
                    .forEach(recipient -> sender.send(event, recipient));
        });
    }
}
