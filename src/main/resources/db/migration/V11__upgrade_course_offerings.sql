DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM course_offerings) THEN
        RAISE EXCEPTION
            'V11 requires course_offerings to be empty before upgrading. Backfill existing records first.';
    END IF;
END $$;

ALTER TABLE course_offerings
    ALTER COLUMN course_code DROP NOT NULL;

ALTER TABLE course_offerings
    ALTER COLUMN course_name DROP NOT NULL;

DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    FOR constraint_record IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'course_offerings'::regclass
          AND contype = 'u'
          AND pg_get_constraintdef(oid) ILIKE '%course_code%'
    LOOP
        EXECUTE format(
            'ALTER TABLE course_offerings DROP CONSTRAINT %I',
            constraint_record.conname
        );
    END LOOP;
END $$;

ALTER TABLE course_offerings
    ADD COLUMN course_id BIGINT,
    ADD COLUMN semester_id BIGINT,
    ADD COLUMN section VARCHAR(20),
    ADD COLUMN lecturer_id BIGINT,
    ADD COLUMN credits SMALLINT,
    ADD COLUMN registered_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN status VARCHAR(12) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE course_offerings
    ALTER COLUMN course_id SET NOT NULL,
    ALTER COLUMN semester_id SET NOT NULL,
    ALTER COLUMN section SET NOT NULL,
    ALTER COLUMN lecturer_id SET NOT NULL,
    ALTER COLUMN credits SET NOT NULL;

ALTER TABLE course_offerings
    ADD CONSTRAINT fk_course_offering_course
        FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_course_offering_semester
        FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_course_offering_lecturer
        FOREIGN KEY (lecturer_id) REFERENCES user_accounts(id) ON DELETE RESTRICT,
    ADD CONSTRAINT uq_course_offering_course_semester_section
        UNIQUE (course_id, semester_id, section),
    ADD CONSTRAINT ck_course_offering_credits
        CHECK (credits > 0),
    ADD CONSTRAINT ck_course_offering_registered_count
        CHECK (registered_count BETWEEN 0 AND capacity),
    ADD CONSTRAINT ck_course_offering_status
        CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED'));

CREATE INDEX idx_course_offering_course
    ON course_offerings(course_id);

CREATE INDEX idx_course_offering_semester
    ON course_offerings(semester_id);

CREATE INDEX idx_course_offering_lecturer
    ON course_offerings(lecturer_id);

CREATE INDEX idx_course_offering_department
    ON course_offerings(course_id);