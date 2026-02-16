-- =========================================================
-- Scenario: High Net Worth Accounts Analysis
-- Purpose : Identifies accounts with a balance higher than the 
--           global bank average. Demonstrates the use of 
--           subqueries for dynamic filtering.
-- =========================================================

SELECT 
    u.first_name, 
    u.last_name, 
    a.iban, 
    a.balance,
    a.currency
FROM users u
JOIN accounts a ON u.id = a.user_id
WHERE a.balance > (SELECT AVG(balance) FROM accounts)
ORDER BY a.balance DESC;