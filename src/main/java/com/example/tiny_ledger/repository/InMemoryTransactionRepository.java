
package com.example.tiny_ledger.repository;

import com.example.tiny_ledger.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryTransactionRepository
        implements TransactionRepository {

    private final ConcurrentMap<UUID, Transaction> transactions =
            new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction cannot be null"
            );
        }

        Transaction existing = transactions.putIfAbsent(
                transaction.transactionId(),
                transaction
        );

        if (existing != null) {
            throw new IllegalArgumentException(
                    "Transaction already exists: "
                            + transaction.transactionId()
            );
        }

        return transaction;
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    @Override
    public List<Transaction> findByAccountId(UUID accountId) {
        return transactions.values()
                .stream()
                .filter(transaction ->
                        transaction.accountId().equals(accountId))
                .sorted(Comparator.comparing(
                        Transaction::createdAt
                ).reversed())
                .toList();
    }

    @Override
    public boolean existsById(UUID transactionId) {
        return transactions.containsKey(transactionId);
    }
}
