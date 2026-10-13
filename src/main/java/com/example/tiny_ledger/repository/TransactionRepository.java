
package com.example.tiny_ledger.repository;

import com.example.tiny_ledger.domain.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID transactionId);

    List<Transaction> findByAccountId(UUID accountId);

    boolean existsById(UUID transactionId);
}
