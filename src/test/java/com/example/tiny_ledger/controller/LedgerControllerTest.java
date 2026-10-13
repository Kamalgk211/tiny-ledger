package com.example.tiny_ledger.controller;

import com.example.tiny_ledger.domain.Account;
import com.example.tiny_ledger.domain.Money;
import com.example.tiny_ledger.domain.Transaction;
import com.example.tiny_ledger.domain.TransactionType;
import com.example.tiny_ledger.service.LedgerService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerController.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LedgerService ledgerService;

    private final UUID accountId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final Currency GBP = Currency.getInstance("GBP");

    @Test
    void createAccount_returns201() throws Exception {
        Account account = new Account(
                accountId,
                userId,
                new Money(new BigDecimal("0.00"), GBP)
        );

        when(ledgerService.createAccount(userId, GBP))
                .thenReturn(account);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "userId": "%s",
                              "currency": "GBP"
                            }
                            """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId")
                        .value(accountId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.currency").value("GBP"));

        verify(ledgerService).createAccount(userId, GBP);
    }

    @Test
    void createAccount_withMissingUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "currency": "GBP"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ledgerService);
    }

    @Test
    void getBalance_returns200() throws Exception {
        when(ledgerService.getBalance(accountId))
                .thenReturn(new Money(
                        new BigDecimal("125.50"), GBP));

        mockMvc.perform(get(
                        "/api/v1/accounts/{accountId}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(125.50))
                .andExpect(jsonPath("$.currency").value("GBP"));

        verify(ledgerService).getBalance(accountId);
    }

    @Test
    void deposit_returns201() throws Exception {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                accountId,
                TransactionType.CREDIT,
                new Money(new BigDecimal("50.00"), GBP),
                "Salary",
                Instant.parse("2026-10-12T09:00:00Z")
        );

        when(ledgerService.deposit(
                eq(accountId), any(Money.class), eq("Salary")))
                .thenReturn(transaction);

        mockMvc.perform(post(
                        "/api/v1/accounts/{accountId}/deposits", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 50.00,
                              "currency": "GBP",
                              "description": "Salary"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.description").value("Salary"));

        verify(ledgerService).deposit(
                eq(accountId),
                any(Money.class),
                eq("Salary")
        );
    }

    @Test
    void deposit_withZeroAmount_returns400() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/accounts/{accountId}/deposits", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 0,
                              "currency": "GBP",
                              "description": "Invalid deposit"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ledgerService);
    }

    @Test
    void withdraw_returns201() throws Exception {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                accountId,
                TransactionType.DEBIT,
                new Money(new BigDecimal("20.00"), GBP),
                "Groceries",
                Instant.parse("2026-10-12T09:30:00Z")
        );

        when(ledgerService.withdraw(
                eq(accountId), any(Money.class), eq("Groceries")))
                .thenReturn(transaction);

        mockMvc.perform(post(
                        "/api/v1/accounts/{accountId}/withdrawals", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 20.00,
                              "currency": "GBP",
                              "description": "Groceries"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.description").value("Groceries"));

        verify(ledgerService).withdraw(
                eq(accountId),
                any(Money.class),
                eq("Groceries")
        );
    }

    @Test
    void getTransactionHistory_returns200() throws Exception {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                accountId,
                TransactionType.CREDIT,
                new Money(new BigDecimal("75.00"), GBP),
                "Refund",
                Instant.parse("2026-10-12T08:00:00Z")
        );

        when(ledgerService.getTransactionHistory(accountId))
                .thenReturn(List.of(transaction));

        mockMvc.perform(get(
                        "/api/v1/accounts/{accountId}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("CREDIT"))
                .andExpect(jsonPath("$[0].amount").value(75.00))
                .andExpect(jsonPath("$[0].description").value("Refund"));

        verify(ledgerService).getTransactionHistory(accountId);
    }
}