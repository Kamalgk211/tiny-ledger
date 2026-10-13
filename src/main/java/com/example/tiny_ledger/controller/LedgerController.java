
package com.example.tiny_ledger.controller;

import com.example.tiny_ledger.dto.AccountResponse;
import com.example.tiny_ledger.dto.BalanceResponse;
import com.example.tiny_ledger.dto.CreateAccountRequest;
import com.example.tiny_ledger.dto.TransactionRequest;
import com.example.tiny_ledger.dto.TransactionResponse;
import com.example.tiny_ledger.domain.Account;
import com.example.tiny_ledger.domain.Money;
import com.example.tiny_ledger.domain.Transaction;
import com.example.tiny_ledger.service.LedgerService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        Currency currency = Currency.getInstance(request.currency());

        Account account = ledgerService.createAccount(
                request.userId(), currency);

        AccountResponse response = new AccountResponse(
                account.getAccountId(),
                account.getUserId(),
                account.getBalance().amount(),
                account.getBalance().currency().getCurrencyCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable java.util.UUID accountId) {

        Money balance = ledgerService.getBalance(accountId);

        return ResponseEntity.ok(new BalanceResponse(
                balance.amount(),
                balance.currency().getCurrencyCode()
        ));
    }

    @PostMapping("/{accountId}/deposits")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable java.util.UUID accountId,
            @Valid @RequestBody TransactionRequest request) {

        Money amount = toMoney(request);

        Transaction transaction = ledgerService.deposit(
                accountId, amount, request.description());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(transaction));
    }

    @PostMapping("/{accountId}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable java.util.UUID accountId,
            @Valid @RequestBody TransactionRequest request) {

        Money amount = toMoney(request);

        Transaction transaction = ledgerService.withdraw(
                accountId, amount, request.description());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(transaction));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable java.util.UUID accountId) {

        List<TransactionResponse> response =
                ledgerService.getTransactionHistory(accountId)
                        .stream()
                        .map(LedgerController::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    private Money toMoney(TransactionRequest request) {
        return new Money(
                request.amount(),
                Currency.getInstance(request.currency())
        );
    }

    private static TransactionResponse toResponse(
            Transaction transaction) {

        return new TransactionResponse(
                transaction.transactionId(),
                transaction.accountId(),
                transaction.type().name(),
                transaction.amount().amount(),
                transaction.amount().currency().getCurrencyCode(),
                transaction.description(),
                transaction.createdAt()
        );
    }
}
