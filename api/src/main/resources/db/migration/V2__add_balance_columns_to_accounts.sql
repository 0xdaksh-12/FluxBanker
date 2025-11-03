-- Fix accounts table schema to match Account entity
ALTER TABLE accounts ADD COLUMN mask VARCHAR(4) NOT NULL DEFAULT '0000';
ALTER TABLE accounts ADD COLUMN current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0;
ALTER TABLE accounts ADD COLUMN available_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0;

-- Remove legacy column
ALTER TABLE accounts DROP COLUMN balance;
