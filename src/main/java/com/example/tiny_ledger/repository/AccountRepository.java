package com.example.tiny_ledger.repository;

import com.example.tiny_ledger.domain.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID accountId);

    boolean existsById(UUID accountId);

    void deleteById(UUID accountId);
}
