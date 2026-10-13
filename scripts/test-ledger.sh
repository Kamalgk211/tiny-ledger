
#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080/api/v1/accounts"
USER_ID="11111111-1111-1111-1111-111111111111"

echo "1. Creating account..."

ACCOUNT_RESPONSE=$(curl -fsS -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"currency\":\"GBP\"}")

echo "$ACCOUNT_RESPONSE"

ACCOUNT_ID=$(echo "$ACCOUNT_RESPONSE" | python3 -c \
  'import json,sys; print(json.load(sys.stdin)["accountId"])')

echo "Account ID: $ACCOUNT_ID"

echo "2. Depositing £100..."
curl -fsS -X POST "$BASE_URL/$ACCOUNT_ID/deposits" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00,"currency":"GBP","description":"Initial deposit"}'
echo

echo "3. Checking balance..."
curl -fsS "$BASE_URL/$ACCOUNT_ID/balance"
echo

echo "4. Withdrawing £25..."
curl -fsS -X POST "$BASE_URL/$ACCOUNT_ID/withdrawals" \
  -H "Content-Type: application/json" \
  -d '{"amount":25.00,"currency":"GBP","description":"Test withdrawal"}'
echo

echo "5. Checking final balance..."
curl -fsS "$BASE_URL/$ACCOUNT_ID/balance"
echo

echo "6. Retrieving transaction history..."
curl -fsS "$BASE_URL/$ACCOUNT_ID/transactions"
echo

echo ". Invalid current £25..."
curl -fsS -X POST "$BASE_URL/$ACCOUNT_ID/withdrawals" \
  -H "Content-Type: application/json" \
  -d '{"amount":25.00,"currency":"USD","description":"Test withdrawal"}'
echo

echo "Ledger workflow completed."
