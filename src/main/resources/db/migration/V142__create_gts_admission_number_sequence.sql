-- GT School Admission numbering foundation.
--
-- Produces the numeric sequence component for references such as:
-- PPIS-2026-ADM-000001
--
-- The school code comes from gts_school_profile.school_code.
-- The year comes from the admission application's academic year.
-- This table stores only the atomic counter.

CREATE TABLE gts_admission_number_sequence (
    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    last_issued_number BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_gts_admission_number_sequence
        PRIMARY KEY (tenant_id, academic_year_id),

    CONSTRAINT ck_gts_admission_number_sequence_positive
        CHECK (last_issued_number >= 0)
);

CREATE INDEX idx_gts_admission_number_sequence_year
    ON gts_admission_number_sequence (academic_year_id);
