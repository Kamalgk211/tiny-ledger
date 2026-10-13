# Tiny Ledger API

A RESTful ledger service built with **Java 21 and Spring Boot** to manage accounts, deposits, withdrawals, balances and transaction history.

## Features

- Create accounts with a specified currency.
- Deposit and withdraw funds.
- Retrieve balances and transaction history.
- Validate amounts and currencies.
- Handle missing accounts and insufficient funds.
- Store accounts and transactions using in-memory repositories.

## Tech Stack

- Java 21, Spring Boot, Gradle
- Jakarta Bean Validation
- JUnit 5, Mockito and Spring Boot Test


## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/accounts` | Create an account |
| GET | `/api/v1/accounts/{id}/balance` | Retrieve balance |
| POST | `/api/v1/accounts/{id}/deposits` | Deposit funds |
| POST | `/api/v1/accounts/{id}/withdrawals` | Withdraw funds |
| GET | `/api/v1/accounts/{id}/transactions` | Retrieve transaction history |

## Assumptions and Design Decisions

- New accounts have a unique ID and a zero balance.
- Each account supports one currency; currency conversion is out of scope.
- Deposit and withdrawal amounts must be positive and use the account's currency.
- Withdrawals cannot exceed the available balance; overdrafts are not supported.
- Monetary calculations use `BigDecimal` rather than floating-point types.
- Successful deposits and withdrawals are recorded with transaction IDs, types and timestamps.
- Transaction history is returned newest first.
- Invalid requests return `400`, missing accounts return `404`, and insufficient funds return `409`.
- Account-level synchronisation coordinates in-process balance updates. It does not provide crash-safe transactions or database-grade durability.
- Data is stored in memory and is lost when the application restarts.
- Authentication, authorisation, idempotency keys and account-to-account transfers are out of scope.

## Testing

Run the tests:

```bash
./gradlew clean test
```

Build the application:

```bash
./gradlew clean build
```

Run the Application

```bash
./gradlew bootRun
```

The API runs at http://localhost:8080.


## API Examples

The following examples use `curl`. Run them against a running application.

### 1. Create an account

```bash
curl -i -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "00000000-0000-0000-0000-000000000001",
    "currency": "GBP"
  }'
```

Copy the returned `accountId` and use it in the following requests.

```bash
export ACCOUNT_ID="replace-with-account-id"
```

### 2. Get account balance

```bash
curl -i \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/balance"
```

### 3. Deposit funds

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/deposits" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "GBP",
    "description": "Initial deposit"
  }'
```

### 4. Withdraw funds

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/withdrawals" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25.00,
    "currency": "GBP",
    "description": "Test withdrawal"
  }'
```

### 5. Get transaction history

```bash
curl -i \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/transactions"
```

### 6. Test insufficient funds

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/withdrawals" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "currency": "GBP",
    "description": "Insufficient funds test"
  }'
```

Expected status: `409 Conflict`.

### 7. Test invalid amount

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/deposits" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": -10.00,
    "currency": "GBP",
    "description": "Invalid deposit"
  }'
```

Expected status: `400 Bad Request`.

### 8. Test currency mismatch

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/accounts/${ACCOUNT_ID}/deposits" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10.00,
    "currency": "USD",
    "description": "Currency mismatch test"
  }'
```

Expected status: `400 Bad Request`.

### 9. Test account not found

```bash
curl -i \
  http://localhost:8080/api/v1/accounts/00000000-0000-0000-0000-000000000099/balance
```

Expected status: `404 Not Found`.

## Limitations

This implementation is intended for a coding assessment. Production use would require durable persistence, database transaction management, stronger concurrency guarantees across multiple application instances, idempotent request handling, authentication and authorisation, and appropriate monitoring.
