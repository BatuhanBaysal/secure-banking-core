-- =========================================================
-- Scenario: Executive & Analytics Reporting (Virtual Tables)
-- Business Case: 
-- Analysts often need a combined view of User, Account, and Address. 
-- Instead of writing complex JOINs repeatedly, we provide a 
-- 'View' to simplify data access and improve developer productivity.
-- =========================================================

-- STEP 1: Execute this block first to create the View
CREATE OR REPLACE VIEW v_customer_financial_summary AS
SELECT 
    u.first_name || ' ' || u.last_name AS full_name,
    u.customer_number,
    a.iban,
    a.balance,
    a.currency,
    ad.city,
    ad.phone_number
FROM users u
JOIN accounts a ON u.id = a.user_id
JOIN addresses ad ON u.address_id = ad.id 
WHERE u.is_active = true;

-- STEP 2: Now you can query it like a real table
SELECT * FROM v_customer_financial_summary 
WHERE city = 'İstanbul' AND balance > 100000;