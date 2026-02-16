-- =========================================================
-- Scenario: High-Activity Sender Accounts
-- Purpose : Identifies accounts that have initiated at least 2 
--           transactions, showing their total outflow for 
--           anti-fraud and loyalty monitoring.
-- =========================================================

SELECT 
    u.first_name, 
    u.last_name, 
    a.iban, 
    COUNT(t.id) AS transaction_count, 
    SUM(t.amount) AS total_amount_sent
FROM users u
JOIN accounts a ON u.id = a.user_id
JOIN transactions t ON a.id = t.sender_account_id
GROUP BY u.id, u.first_name, u.last_name, a.iban
HAVING COUNT(t.id) >= 2
ORDER BY total_amount_sent DESC;