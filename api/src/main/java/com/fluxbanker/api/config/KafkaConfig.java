package com.fluxbanker.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {
    public static final String TOPIC_TRANSACTIONS = "fluxbanker.transactions";

    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name(TOPIC_TRANSACTIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
