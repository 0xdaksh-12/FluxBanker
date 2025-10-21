package com.fluxbanker.api.kafka;

import com.fluxbanker.api.event.TransactionEvent;
import com.fluxbanker.api.metrics.BankingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final BankingMetrics bankingMetrics;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "fluxbanker.transactions", groupId = "fluxbanker-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(TransactionEvent event) {
        log.info("AUDIT event={} eventId={} type={} amount={} source={} dest={} status={} at={}",
                "TRANSACTION_PROCESSED", event.eventId(), event.txType(), event.amount(),
                event.sourceAccountId(), event.destinationAccountId(), event.status(), event.occurredAt());
        
        bankingMetrics.incrementTransfer(event.txType(), event.status());
    }
}
