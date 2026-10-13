package com.example.tiny_ledger.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal amount,
        String currency
) {
}