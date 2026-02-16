-- =========================================================
-- Scenario: Database Performance Optimization (Indexing)
-- =========================================================
-- Business Case: 
-- As the banking system grows to millions of users, searches by 
-- TCKN (National ID) and IBAN become slow, causing high CPU usage 
-- and latency. To ensure sub-second response times, we must 
-- optimize query execution paths.
--
-- Objective:
-- Implement B-Tree indexes on frequently searched unique columns 
-- to replace "Sequential Scans" with "Index Scans".
-- =========================================================

-- 1. Create Index for TCKN to speed up user lookup by ID
CREATE INDEX idx_user_tckn ON users(tckn);

-- 2. Create Index for IBAN to optimize transaction and account queries
CREATE INDEX idx_account_iban ON accounts(iban);

-- 3. Verification: List all indexes on core tables
-- This confirms our manual indexes and auto-generated primary/unique indexes.
SELECT * FROM pg_indexes 
WHERE tablename IN ('users', 'accounts');

-- 4. Performance Audit: Explain Analyze
-- Running this command allows us to see the "Query Plan".
-- We expect to see "Index Scan" instead of "Seq Scan".
EXPLAIN ANALYZE SELECT * FROM users WHERE tckn = '10000000146';

-- =========================================================
-- Expected Result in Execution Plan:
-- "Index Scan using idx_user_tckn on users..."
-- Execution Time: < 0.1ms (Optimized)
-- =========================================================