-- GT-EDU-PROFILE-001
--
-- Education grading profile foundation.
--
-- Supports reusable education framework configurations:
-- Uganda Education System
-- Cambridge
-- IB
-- Montessori
-- Institution-defined frameworks.
--
-- Profiles provision grading schemes.
-- No grading rules are hard-coded.


CREATE TABLE gts_education_grading_profile (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),


    profile_code VARCHAR(100) NOT NULL,

    profile_name VARCHAR(250) NOT NULL,


    country_code VARCHAR(10),

    education_system VARCHAR(100),


    description VARCHAR(1500),


    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_education_grading_profile_code
        UNIQUE (
            tenant_id,
            profile_code
        ),


    CONSTRAINT ck_education_grading_profile_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);


CREATE INDEX ix_education_grading_profile_tenant
ON gts_education_grading_profile(
    tenant_id
);

