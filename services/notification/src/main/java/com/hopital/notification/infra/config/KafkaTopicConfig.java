package com.hopital.notification.infra.config;

import com.hopital.notification.application.config.NotificationProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic notificationRequestsTopic(NotificationProperties properties) {
        return TopicBuilder.name(properties.topic()).partitions(1).replicas(1).build();
    }
}
