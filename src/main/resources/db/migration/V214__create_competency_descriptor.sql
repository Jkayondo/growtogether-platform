-- GT-COMPETENCY-DESCRIPTOR-001
--
-- Configurable competency assessment descriptors.
--
-- Supports:
-- ECD competency reporting
-- CBC competency assessment
-- Montessori observations
-- International competency frameworks.
--
-- No competency wording is hard-coded.


CREATE TABLE gts_competency_descriptor (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),


    grading_scheme_id UUID NOT NULL
        REFERENCES gts_grading_scheme(id)
        ON DELETE CASCADE,


    descriptor_code VARCHAR(100) NOT NULL,

    descriptor_name VARCHAR(250) NOT NULL,


    performance_level VARCHAR(100) NOT NULL,


    description VARCHAR(1500),


    teacher_guidance VARCHAR(2000),


    sequence_number INTEGER NOT NULL,


    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_competency_descriptor_code
        UNIQUE (
            tenant_id,
            grading_scheme_id,
            descriptor_code
        ),


    CONSTRAINT uq_competency_descriptor_sequence
        UNIQUE (
            tenant_id,
            grading_scheme_id,
            sequence_number
        ),


    CONSTRAINT ck_competency_descriptor_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);


CREATE INDEX ix_competency_descriptor_scheme
    ON gts_competency_descriptor(
        tenant_id,
        grading_scheme_id
    );

