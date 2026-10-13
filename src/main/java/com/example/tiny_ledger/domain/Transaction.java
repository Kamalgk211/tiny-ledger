package com.example.tiny_ledger.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Transaction(
        UUID transactionId,
        UUID accountId,
        TransactionType type,
        Money amount,
        String description,
        Instant createdAt
) {
    public Transaction {
        Objects.requireNonNull(
                transactionId, "Transaction ID cannot be null"
        );
        Objects.requireNonNull(
                accountId, "Account ID cannot be null"
        );
        Objects.requireNonNull(
                type, "Transaction type cannot be null"
        );
        Objects.requireNonNull(
                amount, "Amount cannot be null"
        );
        Objects.requireNonNull(
                createdAt, "Creation time cannot be null"
        );

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be positive"
            );
        }

        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 255 characters"
            );
        }
    }
}
