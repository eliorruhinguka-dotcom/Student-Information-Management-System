
ALTER TABLE student
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE student
    ADD COLUMN semester_fee_paid_percentage DOUBLE PRECISION
        NOT NULL DEFAULT 0.0;

ALTER TABLE student
    ADD CONSTRAINT chk_student_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'GRADUATED'));

ALTER TABLE student
    ADD CONSTRAINT chk_student_fee_percentage
        CHECK (semester_fee_paid_percentage >= 0
            AND semester_fee_paid_percentage <= 100);
