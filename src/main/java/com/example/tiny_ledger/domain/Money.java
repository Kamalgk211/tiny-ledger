package com.example.tiny_ledger.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(
        BigDecimal amount,
        Currency currency
){
    public Money{
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if(amount.scale() > 2){
            throw new IllegalArgumentException(
                    "Amount cannot have more than 2 decimall places"
            );
        }
    }

    public Money add(Money other){
        validateCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other){
        validateCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    private void validateCurrency(Money other) {
        Objects.requireNonNull(amount, "Amount cannot be null");

        if(!currency.equals(other.currency)){
            throw new IllegalArgumentException(
                    "Currencies do not match"
            );
        }
    }

}
