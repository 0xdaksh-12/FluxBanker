# Developer Examples

Practical, copy-paste examples for common development tasks in FluxBanker.

---

## Authentication

### Register a new user

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "password": "securepassword"
  }'
```

**Response** — returns an `accessToken` and sets a `refreshToken` HTTP-only cookie:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "uuid",
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "role": "USER"
  }
}
```

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "jane@example.com", "password": "securepassword"}'
```

Store the `accessToken` from the response and pass it as a Bearer token on all subsequent requests.

---

## Accounts

### List accounts for the authenticated user

```bash
curl http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer <accessToken>"
```

### Provision a new savings account

```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Holiday Fund", "subtype": "SAVINGS"}'
```

### Simulate a deposit (dev tooling)

Inject funds into an account without a real payment processor. Useful for local testing.

```bash
curl -X POST http://localhost:8080/api/v1/accounts/<accountId>/deposit \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000.00}'
```

---

## Transactions

### Transfer between two accounts

```bash
curl -X POST http://localhost:8080/api/v1/transactions/transfer \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "<uuid>",
    "destinationAccountId": "<uuid>",
    "amount": 250.00
  }'
```

**Error case — insufficient funds** returns `400 Bad Request`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds in source account."
}
```

### Get transaction history for an account

```bash
curl "http://localhost:8080/api/v1/transactions?accountId=<uuid>" \
  -H "Authorization: Bearer <accessToken>"
```

---

## Admin Endpoints

Admin routes require a user with `role: ADMIN`. Promote a user by adding their email to `ADMIN_EMAILS` in `api/.env` before they register.

### List all users

```bash
curl http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <adminAccessToken>"
```

### List all transactions (platform-wide)

```bash
curl http://localhost:8080/api/v1/admin/transactions \
  -H "Authorization: Bearer <adminAccessToken>"
```

---

## Token Refresh

The `refreshToken` is stored in an HTTP-only cookie and is sent automatically by the browser. To refresh the access token manually (e.g. in a script):

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Cookie: refreshToken=<token>"
```

---

## Querying Metrics (Prometheus)

```bash
# Total completed transfers
curl 'http://localhost:9090/api/v1/query?query=banking_transfers_total'

# Active provisioned accounts
curl 'http://localhost:9090/api/v1/query?query=banking_accounts_active'
```

---

## Kafka Event Inspection

```bash
# Tail the transactions topic from inside the Kafka container
docker exec -it fluxbanker-kafka \
  kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic fluxbanker.transactions \
  --from-beginning
```

---

## Redis Cache Inspection

```bash
# Connect to Redis CLI
docker exec -it fluxbanker-redis redis-cli

# List all cached account keys
KEYS userAccounts::*

# Inspect a cached entry
GET "userAccounts::<userId>"
```
