ALTER TABLE course_registrations
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED';

ALTER TABLE course_registrations
    ADD COLUMN cancelled_at TIMESTAMP;

ALTER TABLE course_registrations
    ADD CONSTRAINT chk_course_registration_status
        CHECK (status IN ('REGISTERED', 'CANCELLED'));

ALTER TABLE course_registrations
    ADD CONSTRAINT chk_course_registration_cancelled_at
        CHECK (
            (status = 'REGISTERED' AND cancelled_at IS NULL)
                OR
            (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
            );

ALTER TABLE course_registrations
DROP CONSTRAINT uk_registration_student_offering;

CREATE UNIQUE INDEX uk_registration_active_student_offering
    ON course_registrations (student_id, course_offering_id)
    WHERE status = 'REGISTERED';