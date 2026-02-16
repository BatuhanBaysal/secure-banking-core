-- =========================================================
-- Scenario: High-Balance Foreign Currency Accounts
-- Purpose : Lists customers with USD balances exceeding 50,000 
--           units for risk analysis and premium service targeting.
-- =========================================================

SELECT * FROM accounts 
WHERE balance > 50000 
  AND currency = 'USD'
ORDER BY balance DESC;