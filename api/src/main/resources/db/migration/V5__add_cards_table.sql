-- Fix users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Create Cards table
CREATE TABLE IF NOT EXISTS cards (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    user_id UUID NOT NULL,
    card_number VARCHAR(19) NOT NULL UNIQUE,
    expiry_date VARCHAR(5) NOT NULL,
    cvv VARCHAR(4) NOT NULL,
    pin_hash VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    subtype VARCHAR(20) NOT NULL,
    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create Loan Details table
CREATE TABLE IF NOT EXISTS loan_details (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE,
    original_principal DECIMAL(19, 4) NOT NULL,
    interest_rate DECIMAL(5, 4) NOT NULL,
    term_months INTEGER NOT NULL,
    monthly_payment DECIMAL(19, 4) NOT NULL,
    next_payment_due_date DATE NOT NULL,
    CONSTRAINT fk_loan_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Create Credit Details table
CREATE TABLE IF NOT EXISTS credit_details (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE,
    credit_limit DECIMAL(19, 4) NOT NULL,
    apr DECIMAL(5, 4) NOT NULL,
    statement_balance DECIMAL(19, 4) NOT NULL,
    minimum_payment_due DECIMAL(19, 4) NOT NULL,
    next_payment_due_date DATE NOT NULL,
    CONSTRAINT fk_credit_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);
