
package com.example.tiny_ledger.service;

import com.example.tiny_ledger.domain.Account;
import com.example.tiny_ledger.domain.Money;
import com.example.tiny_ledger.domain.Transaction;
import com.example.tiny_ledger.domain.TransactionType;
import com.example.tiny_ledger.exception.AccountNotFoundException;
import com.example.tiny_ledger.exception.CurrencyMismatchException;
import com.example.tiny_ledger.exception.InsufficientBalanceException;
import com.example.tiny_ledger.repository.AccountRepository;
import com.example.tiny_ledger.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    private UUID accountId;
    private UUID userId;
    private Currency gbp;
    private Account account;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();
        gbp = Currency.getInstance("GBP");

        account = new Account(
                accountId,
                userId,
                money("100.00")
        );

        lenient().when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Money money(String amount) {
        return new Money(new BigDecimal(amount), gbp);
    }

    @Test
    void shouldCreateAccountWithZeroBalance() {
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account created = ledgerService.createAccount(userId, gbp);

        assertNotNull(created.getAccountId());
        assertEquals(userId, created.getUserId());
        assertEquals(
                0,
                created.getBalance().amount().compareTo(BigDecimal.ZERO)
        );
        assertEquals(gbp, created.getBalance().currency());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldRejectNullUserIdWhenCreatingAccount() {
        assertThrows(
                NullPointerException.class,
                () -> ledgerService.createAccount(null, gbp)
        );

        verifyNoInteractions(accountRepository);
    }

    @Test
    void shouldRetrieveAccountBalance() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        Money balance = ledgerService.getBalance(accountId);

        assertEquals(0, balance.amount()
                .compareTo(new BigDecimal("100.00")));
    }

    @Test
    void shouldRejectBalanceLookupForMissingAccount() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> ledgerService.getBalance(accountId)
        );
    }

    @Test
    void shouldDepositMoneyAndCreateCreditTransaction() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        Transaction transaction = ledgerService.deposit(
                accountId,
                money("25.00"),
                "Salary"
        );

        assertEquals(
                0,
                account.getBalance().amount()
                        .compareTo(new BigDecimal("125.00"))
        );
        assertEquals(TransactionType.CREDIT, transaction.type());
        assertEquals(accountId, transaction.accountId());
        assertEquals("Salary", transaction.description());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldWithdrawMoneyAndCreateDebitTransaction() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        Transaction transaction = ledgerService.withdraw(
                accountId,
                money("30.00"),
                "Shopping"
        );

        assertEquals(
                0,
                account.getBalance().amount()
                        .compareTo(new BigDecimal("70.00"))
        );
        assertEquals(TransactionType.DEBIT, transaction.type());
        assertEquals("Shopping", transaction.description());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRejectWithdrawalWhenFundsAreInsufficient() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> ledgerService.withdraw(
                        accountId,
                        money("150.00"),
                        "Too expensive"
                )
        );

        assertEquals(
                0,
                account.getBalance().amount()
                        .compareTo(new BigDecimal("100.00"))
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldRejectZeroDeposit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ledgerService.deposit(
                        accountId,
                        money("0.00"),
                        "Invalid deposit"
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldRejectNegativeWithdrawal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ledgerService.withdraw(
                        accountId,
                        money("-10.00"),
                        "Invalid withdrawal"
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldRejectCurrencyMismatch() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        Money usdAmount = new Money(
                new BigDecimal("10.00"),
                Currency.getInstance("USD")
        );

        assertThrows(
                CurrencyMismatchException.class,
                () -> ledgerService.deposit(
                        accountId,
                        usdAmount,
                        "Wrong currency"
                )
        );

        assertEquals(
                0,
                account.getBalance().amount()
                        .compareTo(new BigDecimal("100.00"))
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldRejectDepositForMissingAccount() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> ledgerService.deposit(
                        accountId,
                        money("10.00"),
                        "Deposit"
                )
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldReturnTransactionHistory() {
        Transaction older = new Transaction(
                UUID.randomUUID(),
                accountId,
                TransactionType.CREDIT,
                money("10.00"),
                "First",
                Instant.parse("2026-10-12T08:00:00Z")
        );

        Transaction newer = new Transaction(
                UUID.randomUUID(),
                accountId,
                TransactionType.DEBIT,
                money("5.00"),
                "Second",
                Instant.parse("2026-10-12T09:00:00Z")
        );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByAccountId(accountId))
                .thenReturn(List.of(newer, older));

        List<Transaction> history =
                ledgerService.getTransactionHistory(accountId);

        assertEquals(2, history.size());
        assertEquals(newer, history.get(0));
        assertEquals(older, history.get(1));

        verify(transactionRepository).findByAccountId(accountId);
    }

    @Test
    void shouldRestoreBalanceWhenTransactionSaveFails() {
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Storage failure"));

        assertThrows(
                RuntimeException.class,
                () -> ledgerService.deposit(
                        accountId,
                        money("25.00"),
                        "Deposit"
                )
        );

        assertEquals(
                0,
                account.getBalance().amount()
                        .compareTo(new BigDecimal("100.00"))
        );
    }
}
