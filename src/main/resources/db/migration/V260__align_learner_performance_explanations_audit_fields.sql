ALTER TABLE learner_performance_explanations
    ADD COLUMN created_by VARCHAR(150),
    ADD COLUMN updated_by VARCHAR(150),
    ADD COLUMN version BIGINT,
    ADD COLUMN status VARCHAR(20);

UPDATE learner_performance_explanations
SET
    created_by = COALESCE(created_by, 'migration-v260'),
    updated_by = COALESCE(updated_by, 'migration-v260'),
    version = COALESCE(version, 0),
    status = COALESCE(status, 'ACTIVE');

ALTER TABLE learner_performance_explanations
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN updated_by SET NOT NULL,
    ALTER COLUMN version SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE learner_performance_explanations
    ALTER COLUMN created_at
        TYPE TIMESTAMPTZ
        USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at
        TYPE TIMESTAMPTZ
        USING updated_at AT TIME ZONE 'UTC';
