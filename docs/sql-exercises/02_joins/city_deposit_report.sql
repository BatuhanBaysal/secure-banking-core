-- =========================================================
-- Scenario: Total Balance Analysis by City
-- Purpose : Aggregates total bank deposits for each city to 
--           determine regional growth and branch opportunities.
-- =========================================================

SELECT  
	ad.city,
	SUM(ac.balance) AS total_deposit,
	COUNT(u.id) AS total_customers
FROM addresses ad
JOIN users u ON ad.id = u.id
JOIN accounts ac ON u.id = ac.user_id
GROUP BY ad.city
ORDER BY total_deposit DESC;
