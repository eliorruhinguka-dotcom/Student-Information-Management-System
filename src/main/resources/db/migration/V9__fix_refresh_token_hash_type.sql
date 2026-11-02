-- ============================================================
-- V9: Fix refresh token hash column type
-- ============================================================

ALTER TABLE refresh_token
ALTER COLUMN token_hash TYPE VARCHAR(64)
    USING RTRIM(token_hash)::VARCHAR(64);