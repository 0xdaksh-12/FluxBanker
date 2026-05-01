-- Performance indexes for FluxBanker
-- Added to resolve query lagging in production

-- Index for fetching user accounts (Dashboard)
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);

-- Index for fetching transactions by account (Transaction History)
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);

-- Index for sorting transactions by timestamp (ordering)
CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp DESC);

-- Index for session lookups by user
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);

-- Index for card lookups by user
CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id);

-- Index for card lookups by account
CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
