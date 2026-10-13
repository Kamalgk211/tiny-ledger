
package com.example.tiny_ledger.service;

import com.example.tiny_ledger.domain.Account;
import com.example.tiny_ledger.domain.Money;
import com.example.tiny_ledger.domain.Transaction;
import com.example.tiny_ledger.domain.TransactionType;
import com.example.tiny_ledger.exception.AccountNotFoundException;
import com.example.tiny_ledger.exception.CurrencyMismatchException;
import com.example.tiny_ledger.repository.AccountRepository;
import com.example.tiny_ledger.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public LedgerServiceImpl(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Account createAccount(UUID userId, Currency currency) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        Money openingBalance =
                new Money(new BigDecimal("0.00"), currency);

        Account account = new Account(
                UUID.randomUUID(),
                userId,
                openingBalance
        );

        return accountRepository.save(account);
    }

    @Override
    public Money getBalance(UUID accountId) {
        Account account = findAccount(accountId);
        return account.getBalance();
    }

    @Override
    public Transaction deposit(
            UUID accountId,
            Money amount,
            String description
    ) {
        validateAmount(amount);

        Account account = findAccount(accountId);

        synchronized (account) {
            validateCurrency(account, amount);

            account.credit(amount);

            try {
                Transaction transaction = createTransaction(
                        accountId,
                        TransactionType.CREDIT,
                        amount,
                        description
                );

                return transactionRepository.save(transaction);
            } catch (RuntimeException exception) {
                // Roll back the in-memory balance update.
                account.debit(amount);
                throw exception;
            }
        }
    }

    @Override
    public Transaction withdraw(
            UUID accountId,
            Money amount,
            String description
    ) {
        validateAmount(amount);

        Account account = findAccount(accountId);

        synchronized (account) {
            validateCurrency(account, amount);

            // Account.debit rejects insufficient funds.
            account.debit(amount);

            try {
                Transaction transaction = createTransaction(
                        accountId,
                        TransactionType.DEBIT,
                        amount,
                        description
                );

                return transactionRepository.save(transaction);
            } catch (RuntimeException exception) {
                // Restore the balance if transaction saving fails.
                account.credit(amount);
                throw exception;
            }
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(UUID accountId) {
        findAccount(accountId);

        return transactionRepository.findByAccountId(accountId);
    }

    private Account findAccount(UUID accountId) {
        Objects.requireNonNull(
                accountId, "Account ID cannot be null"
        );

        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountId)
                );
    }

    private void validateAmount(Money amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }

    private void validateCurrency(Account account, Money amount) {
        if (!account.getBalance().currency()
                .equals(amount.currency())) {
            throw new CurrencyMismatchException(account.getBalance().currency().toString(),amount.currency().toString());
        }
    }

    private Transaction createTransaction(
            UUID accountId,
            TransactionType type,
            Money amount,
            String description
    ) {
        return new Transaction(
                UUID.randomUUID(),
                accountId,
                type,
                amount,
                description,
                Instant.now()
        );
    }
}
