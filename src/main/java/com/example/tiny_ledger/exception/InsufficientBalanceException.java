
package com.example.tiny_ledger.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(
            BigDecimal requested,
            BigDecimal available) {
        super("Insufficient balance. Requested: "
                + requested + ", available: " + available);
    }
}
