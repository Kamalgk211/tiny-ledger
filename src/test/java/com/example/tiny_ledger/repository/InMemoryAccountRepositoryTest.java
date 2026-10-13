
package com.example.tiny_ledger.repository;

import com.example.tiny_ledger.domain.Account;
import com.example.tiny_ledger.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountRepositoryTest {

    private AccountRepository repository;
    private Account account;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();

        account = new Account(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Money(
                        new BigDecimal("100.00"),
                        Currency.getInstance("GBP")
                )
        );
    }

    @Test
    void shouldSaveAndFindAccount() {
        repository.save(account);

        Optional<Account> result =
                repository.findById(account.getAccountId());

        assertTrue(result.isPresent());
        assertSame(account, result.get());
    }

    @Test
    void shouldReturnEmptyWhenAccountDoesNotExist() {
        assertTrue(
                repository.findById(UUID.randomUUID()).isEmpty()
        );
    }

    @Test
    void shouldReturnTrueWhenAccountExists() {
        repository.save(account);

        assertTrue(
                repository.existsById(account.getAccountId())
        );
    }

    @Test
    void shouldReturnFalseWhenAccountDoesNotExist() {
        assertFalse(
                repository.existsById(UUID.randomUUID())
        );
    }

    @Test
    void shouldDeleteAccount() {
        repository.save(account);

        repository.deleteById(account.getAccountId());

        assertTrue(
                repository.findById(account.getAccountId()).isEmpty()
        );
        assertFalse(
                repository.existsById(account.getAccountId())
        );
    }

    @Test
    void shouldAllowDeletingNonExistentAccount() {
        assertDoesNotThrow(
                () -> repository.deleteById(UUID.randomUUID())
        );
    }

    @Test
    void shouldRejectNullAccount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
    }

    @Test
    void shouldReplaceExistingAccountWithSameId() {
        repository.save(account);

        Account replacement = new Account(
                account.getAccountId(),
                account.getUserId(),
                new Money(
                        new BigDecimal("200.00"),
                        Currency.getInstance("GBP")
                )
        );

        repository.save(replacement);

        assertSame(
                replacement,
                repository.findById(account.getAccountId()).orElseThrow()
        );
    }
}
