
package com.example.tiny_ledger.service;

import com.example.tiny_ledger.domain.Account;
import com.example.tiny_ledger.domain.Money;
import com.example.tiny_ledger.domain.Transaction;

import java.util.List;
import java.util.Currency;
import java.util.UUID;

public interface LedgerService {

    Account createAccount(UUID userId, Currency currency);

    Money getBalance(UUID accountId);

    Transaction deposit(
            UUID accountId,
            Money amount,
            String description
    );

    Transaction withdraw(
            UUID accountId,
            Money amount,
            String description
    );

    List<Transaction> getTransactionHistory(UUID accountId);
}
