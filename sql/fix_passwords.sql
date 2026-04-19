-- ============================================================
--  FIX: Update password hashes for Judge and Clerk accounts
--  Run this against your existing judicial_system_db to fix login.
-- ============================================================

USE judicial_system_db;

-- judge_sharma and judge_patel: password = "judge123"
UPDATE users SET password_hash = '94358d5abe1d055c1ced4403bb0e397edf8c905b33e03b34ad1b1d3adf2d9cf4'
WHERE username IN ('judge_sharma', 'judge_patel');

-- clerk_raj: password = "clerk123"
UPDATE users SET password_hash = 'a3630b8b8f6c82d33b0695f77f915e69ed7b0c5214062f8b870219845e069d30'
WHERE username = 'clerk_raj';

SELECT user_id, username, role, LEFT(password_hash, 16) AS hash_prefix FROM users;
