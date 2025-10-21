# Redis Caching Layer

To ensure high performance for read-heavy operations, FluxBanker utilizes Redis to cache account information.

## Strategy: Cache-Aside with Write-Eviction

1. **Read Path**: When the frontend requests a user's accounts, Spring checks Redis. If missing (`Cache Miss`), it queries Postgres and caches the result for 5 minutes.
2. **Write Path**: When an account is created or modified, Spring executes the database update and immediately evicts (`@CacheEvict`) the cached entry for that user to prevent stale data.

## Sequence Diagram

```mermaid
sequenceDiagram
    participant React UI
    participant AccountService
    participant Redis
    participant PostgreSQL

    %% Cache Hit
    React UI->>AccountService: getAccountsForUser(userId)
    AccountService->>Redis: GET userAccounts::userId
    Redis-->>AccountService: [AccountData...] (Hit)
    AccountService-->>React UI: Return Accounts

    %% Cache Miss
    React UI->>AccountService: getAccountsForUser(otherUserId)
    AccountService->>Redis: GET userAccounts::otherUserId
    Redis-->>AccountService: null (Miss)
    AccountService->>PostgreSQL: SELECT * FROM accounts WHERE user_id = ...
    PostgreSQL-->>AccountService: [AccountData...]
    AccountService->>Redis: SET userAccounts::otherUserId (TTL 5min)
    AccountService-->>React UI: Return Accounts

    %% Cache Eviction
    React UI->>AccountService: provisionAccount(userId, ...)
    AccountService->>PostgreSQL: INSERT INTO accounts...
    PostgreSQL-->>AccountService: Success
    AccountService->>Redis: DEL userAccounts::userId (Evict)
    AccountService-->>React UI: Return New Account
```
