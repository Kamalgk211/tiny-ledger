package com.example.tiny_ledger.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        UUID userId,
        BigDecimal balance,
        String currency
) {
}