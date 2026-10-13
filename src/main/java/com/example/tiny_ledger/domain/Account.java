package com.example.tiny_ledger.domain;

import com.example.tiny_ledger.exception.InsufficientBalanceException;

import java.util.Objects;
import java.util.UUID;

public class Account {

    private final UUID accountId;
    private final UUID userId;
    private Money balance;

    public Account(UUID accountId, UUID userId, Money openingBalance){
        this.accountId = Objects.requireNonNull(accountId, "AccountId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.balance = Objects.requireNonNull(openingBalance, "Opening balance cannot be null");

        if(openingBalance.amount().signum() < 0){
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }

    }

    public UUID getAccountId() {
        return accountId;
    }
    public UUID getUserId() {
        return userId;
    }

    public Money getBalance() {
        return balance;
    }

    public void credit(Money other){
        validateCurrency(other);
        validatePositiveAmount(other);
        balance = balance.add(other);
    }

    public void debit(Money other){
        validateCurrency(other);
        validatePositiveAmount(other);

        if(balance.amount().compareTo(other.amount()) < 0){
            throw new InsufficientBalanceException(other.amount(), balance.amount());
        }
        balance = balance.subtract(other);
    }

    private void validateCurrency(Money other) {
        Objects.requireNonNull(other, "Amount cannot be null");
        if(!balance.currency().equals(other.currency())){
            throw new IllegalArgumentException("Account currency does not match");
        }

    }

    private void validatePositiveAmount(Money other) {
        Objects.requireNonNull(other, "Amount cannot be null");
        if(other.amount().signum() <= 0){
            throw new IllegalArgumentException("Amount cannot be negative or zero");
        }
    }
}
