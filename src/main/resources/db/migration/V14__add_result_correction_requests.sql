-- V14: Support append-only lecturer correction requests in the existing result correction history.
-- Existing rows are completed corrections and are backfilled as COMPLETED.
-- Do not edit V1-V13; this is a new migration.

ALTER TABLE result_correction
    ADD COLUMN request_status VARCHAR(12) NOT NULL DEFAULT 'COMPLETED';

ALTER TABLE result_correction
    ALTER COLUMN authorised_by DROP NOT NULL;

ALTER TABLE result_correction
    ADD CONSTRAINT ck_result_correction_request_status
        CHECK (request_status IN ('REQUESTED', 'COMPLETED'));

ALTER TABLE result_correction
    ADD CONSTRAINT ck_result_correction_authorisation_state
        CHECK (
            (request_status = 'REQUESTED' AND requested_by IS NOT NULL AND authorised_by IS NULL)
            OR (request_status = 'COMPLETED' AND authorised_by IS NOT NULL)
        );

CREATE INDEX idx_result_correction_status_time
    ON result_correction(request_status, corrected_at DESC);
