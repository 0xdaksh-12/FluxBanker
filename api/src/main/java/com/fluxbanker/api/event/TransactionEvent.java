package com.fluxbanker.api.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEvent(
        UUID eventId,
        String txType,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String status,
        Instant occurredAt
) {
    public static TransactionEvent transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
        return new TransactionEvent(UUID.randomUUID(), "TRANSFER", sourceAccountId, destinationAccountId, amount, "COMPLETED", Instant.now());
    }

    public static TransactionEvent deposit(UUID accountId, BigDecimal amount) {
        return new TransactionEvent(UUID.randomUUID(), "DEPOSIT", null, accountId, amount, "COMPLETED", Instant.now());
    }

    public static TransactionEvent withdrawal(UUID accountId, BigDecimal amount) {
        return new TransactionEvent(UUID.randomUUID(), "WITHDRAWAL", accountId, null, amount, "COMPLETED", Instant.now());
    }
}
