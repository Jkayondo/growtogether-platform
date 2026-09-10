-- GT-GRADING-FOUNDATION-001
--
-- Extends existing gts_grading_scheme foundation created in V038.
--
-- Does NOT recreate gts_grading_scheme.
-- Adds curriculum-aware grading intelligence support.


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS grade_scale_type VARCHAR(50);


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS curriculum_version_id UUID;


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS education_level_id UUID;


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS uses_marks BOOLEAN NOT NULL DEFAULT TRUE;


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS uses_comments BOOLEAN NOT NULL DEFAULT FALSE;


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS uses_grade_points BOOLEAN NOT NULL DEFAULT FALSE;


ALTER TABLE gts_grading_scheme
    ADD COLUMN IF NOT EXISTS uses_aggregation BOOLEAN NOT NULL DEFAULT FALSE;


UPDATE gts_grading_scheme
SET grade_scale_type =
    CASE
        WHEN scheme_type = 'LETTER_GRADE'
            THEN 'LETTER'

        WHEN scheme_type = 'COMPETENCY'
            THEN 'COMPETENCY'

        WHEN scheme_type = 'PERCENTAGE'
            THEN 'PERCENTAGE'

        WHEN scheme_type = 'POINTS'
            THEN 'NUMERIC'

        ELSE 'CUSTOM'
    END
WHERE grade_scale_type IS NULL;


ALTER TABLE gts_grading_scheme
    DROP CONSTRAINT IF EXISTS ck_gts_grading_scheme_type;


ALTER TABLE gts_grading_scheme
    ADD CONSTRAINT ck_gts_grading_scheme_scale
    CHECK (
        grade_scale_type IN (
            'LETTER',
            'NUMERIC',
            'COMPETENCY',
            'PERCENTAGE',
            'CUSTOM'
        )
    );


CREATE INDEX IF NOT EXISTS ix_gts_grading_scheme_curriculum
    ON gts_grading_scheme(
        tenant_id,
        curriculum_version_id
    );


CREATE INDEX IF NOT EXISTS ix_gts_grading_scheme_level
    ON gts_grading_scheme(
        tenant_id,
        education_level_id
    );
