-- =========================================================
-- Scenario: Passive Customer Detection
-- Purpose : Identifies customers who have an account but have 
--           NEVER initiated a transaction. This helps the 
--           marketing team target "welcome" campaigns.
-- =========================================================

SELECT 
    u.first_name, 
    u.last_name, 
    a.iban, 
    a.balance,
    t.id AS transaction_id 
FROM users u
JOIN accounts a ON u.id = a.user_id
LEFT JOIN transactions t ON a.id = t.sender_account_id
WHERE t.id IS NULL;