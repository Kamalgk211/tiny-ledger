
package com.example.tiny_ledger.exception;

public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(
            String accountCurrency,
            String requestedCurrency) {
        super("Currency mismatch. Account currency: "
                + accountCurrency + ", requested currency: "
                + requestedCurrency);
    }
}
