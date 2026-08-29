-- GT School permanent guardian numbering foundation.
--
-- Produces the numeric sequence component for references such as:
-- PPIS-GDN-000001
--
-- Unlike admission application numbering, guardian identity is permanent
-- and therefore is NOT scoped to an academic year.
--
-- The school code comes from gts_school_profile.school_code.
-- This table stores only the atomic tenant-wide counter.

CREATE TABLE gts_guardian_number_sequence (
    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    last_issued_number BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_gts_guardian_number_sequence
        PRIMARY KEY (tenant_id),

    CONSTRAINT ck_gts_guardian_number_sequence_positive
        CHECK (last_issued_number >= 0)
);
