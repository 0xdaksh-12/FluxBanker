package com.fluxbanker.api.dto;

import com.fluxbanker.api.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionDto {
    private UUID id;
    private UUID accountId;
    private BigDecimal amount;
    private Transaction.Type type;
    private Transaction.Status status;
    private String category;
    private String counterpartyName;
    private Instant timestamp;
}
