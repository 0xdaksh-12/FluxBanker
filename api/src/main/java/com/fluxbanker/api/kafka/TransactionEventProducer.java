package com.fluxbanker.api.kafka;

import com.fluxbanker.api.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void publish(TransactionEvent event) {
        kafkaTemplate.send("fluxbanker.transactions", event.eventId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish TransactionEvent eventId={} type={}: {}",
                                event.eventId(), event.txType(), ex.getMessage());
                    } else {
                        log.debug("Published TransactionEvent eventId={} type={} partition={} offset={}",
                                event.eventId(), event.txType(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
