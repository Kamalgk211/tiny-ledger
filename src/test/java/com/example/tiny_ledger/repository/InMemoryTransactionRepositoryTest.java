
package com.example.tiny_ledger.repository;

import com.example.tiny_ledger.domain.Money;
import com.example.tiny_ledger.domain.Transaction;
import com.example.tiny_ledger.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {

    private TransactionRepository repository;
    private UUID accountId;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        accountId = UUID.randomUUID();
        gbp = Currency.getInstance("GBP");
    }

    private Transaction createTransaction(
            UUID transactionId,
            Instant createdAt,
            String amount
    ) {
        return new Transaction(
                transactionId,
                accountId,
                TransactionType.CREDIT,
                new Money(new BigDecimal(amount), gbp),
                "Test transaction",
                createdAt
        );
    }

    @Test
    void shouldSaveAndFindTransaction() {
        Transaction transaction = createTransaction(
                UUID.randomUUID(),
                Instant.parse("2026-10-12T09:00:00Z"),
                "25.00"
        );

        repository.save(transaction);

        assertEquals(
                transaction,
                repository.findById(transaction.transactionId())
                        .orElseThrow()
        );
    }

    @Test
    void shouldReturnEmptyForUnknownTransaction() {
        assertTrue(
                repository.findById(UUID.randomUUID()).isEmpty()
        );
    }

    @Test
    void shouldReturnTrueWhenTransactionExists() {
        Transaction transaction = createTransaction(
                UUID.randomUUID(),
                Instant.now(),
                "10.00"
        );

        repository.save(transaction);

        assertTrue(
                repository.existsById(transaction.transactionId())
        );
    }

    @Test
    void shouldReturnFalseWhenTransactionDoesNotExist() {
        assertFalse(
                repository.existsById(UUID.randomUUID())
        );
    }

    @Test
    void shouldReturnTransactionsForAccountNewestFirst() {
        Transaction older = createTransaction(
                UUID.randomUUID(),
                Instant.parse("2026-10-12T08:00:00Z"),
                "10.00"
        );

        Transaction newer = createTransaction(
                UUID.randomUUID(),
                Instant.parse("2026-10-12T10:00:00Z"),
                "20.00"
        );

        Transaction middle = createTransaction(
                UUID.randomUUID(),
                Instant.parse("2026-10-12T09:00:00Z"),
                "15.00"
        );

        repository.save(older);
        repository.save(newer);
        repository.save(middle);

        List<Transaction> result =
                repository.findByAccountId(accountId);

        assertEquals(
                List.of(newer, middle, older),
                result
        );
    }

    @Test
    void shouldReturnEmptyHistoryForAccountWithoutTransactions() {
        assertTrue(
                repository.findByAccountId(UUID.randomUUID()).isEmpty()
        );
    }

    @Test
    void shouldRejectDuplicateTransactionId() {
        UUID transactionId = UUID.randomUUID();

        Transaction first = createTransaction(
                transactionId,
                Instant.now(),
                "10.00"
        );

        Transaction duplicate = createTransaction(
                transactionId,
                Instant.now(),
                "20.00"
        );

        repository.save(first);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(duplicate)
        );

        assertEquals(
                first,
                repository.findById(transactionId).orElseThrow()
        );
    }

    @Test
    void shouldRejectNullTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
    }

    @Test
    void shouldNotReturnTransactionsFromAnotherAccount() {
        UUID otherAccountId = UUID.randomUUID();

        Transaction otherTransaction = new Transaction(
                UUID.randomUUID(),
                otherAccountId,
                TransactionType.CREDIT,
                new Money(new BigDecimal("10.00"), gbp),
                "Other account",
                Instant.now()
        );

        repository.save(otherTransaction);

        assertTrue(repository.findByAccountId(accountId).isEmpty());
    }
}
