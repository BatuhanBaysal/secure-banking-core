-- =========================================================
-- Scenario: Financial Integrity Constraints
-- Business Case: 
-- In a banking system, we must prevent "impossible" data at the 
-- database level. For example, a standard savings account 
-- should never have a negative balance, and limits cannot be negative.
-- This acts as a last line of defense against application bugs.
-- =========================================================

-- 1. Ensure balance never drops below zero (for standard accounts)
ALTER TABLE accounts 
ADD CONSTRAINT chk_balance_not_negative CHECK (balance >= 0);

-- 2. Ensure daily transaction limits are always positive or zero
ALTER TABLE accounts 
ADD CONSTRAINT chk_daily_limit_positive CHECK (daily_limit >= 0);

-- 3. Verify Constraints: Try to insert an invalid record (This should fail)
-- INSERT INTO accounts (iban, balance, daily_limit, ...) VALUES ('TR00...', -100, -500, ...);