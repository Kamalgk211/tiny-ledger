package com.example.tiny_ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        UUID accountId,
        String type,
        BigDecimal amount,
        String currency,
        String description,
        Instant createdAt
) {
}