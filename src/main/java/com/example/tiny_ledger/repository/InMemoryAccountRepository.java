
package com.example.tiny_ledger.repository;

import com.example.tiny_ledger.domain.Account;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final ConcurrentMap<UUID, Account> accounts =
            new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException(
                    "Account cannot be null"
            );
        }

        accounts.put(account.getAccountId(), account);
        return account;
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public boolean existsById(UUID accountId) {
        return accounts.containsKey(accountId);
    }

    @Override
    public void deleteById(UUID accountId) {
        accounts.remove(accountId);
    }
}
