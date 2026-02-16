-- =========================================================
-- Scenario: Audit-Safe Data Removal (Soft Delete)
-- Business Case: 
-- In banking, we NEVER truly delete a user or account (Hard Delete) 
-- because we must preserve transaction history for legal audits (e.g., BRSA/BDDK regulations). 
-- Instead, we "deactivate" them.
-- =========================================================

-- 1. Deactivating a user instead of deleting
UPDATE users 
SET is_active = false 
WHERE customer_number = 'CUST9001';

-- 2. Automatically deactivating associated accounts
UPDATE accounts 
SET is_active = false 
WHERE user_id = (SELECT id FROM users WHERE customer_number = 'CUST9001');

-- 3. Querying only 'Live' data for the UI
SELECT * FROM users WHERE is_active = true;