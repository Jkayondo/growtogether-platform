CREATE TABLE gts_timetable_generation_request (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    generation_code VARCHAR(100) NOT NULL,

    academic_year_id UUID NOT NULL,
    academic_term_id UUID,
    campus_id UUID NOT NULL,
    bell_schedule_id UUID NOT NULL,

    timetable_type VARCHAR(30) NOT NULL,

    effective_from DATE NOT NULL,
    effective_to DATE,

    generation_mode VARCHAR(30) NOT NULL,

    model_code VARCHAR(100),

    objectives TEXT,

    generation_status VARCHAR(30) NOT NULL
        DEFAULT 'DRAFT',

    eaif_request_id UUID,

    result_timetable_id UUID,

    requested_by UUID NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,

    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    failure_reason TEXT,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT uq_gts_timetable_generation_code
        UNIQUE (tenant_id, generation_code),

    CONSTRAINT ck_gts_timetable_generation_mode
        CHECK (
            generation_mode IN (
                'RULE_ENGINE',
                'AI_ASSISTED'
            )
        ),

    CONSTRAINT ck_gts_timetable_generation_status
        CHECK (
            generation_status IN (
                'DRAFT',
                'READY',
                'GENERATING',
                'GENERATED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_timetable_generation_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        )
);

CREATE INDEX ix_gts_timetable_generation_status
    ON gts_timetable_generation_request (
        tenant_id,
        generation_status
    );

CREATE INDEX ix_gts_timetable_generation_scope
    ON gts_timetable_generation_request (
        tenant_id,
        academic_year_id,
        academic_term_id,
        campus_id
    );

CREATE INDEX ix_gts_timetable_generation_eaif
    ON gts_timetable_generation_request (
        tenant_id,
        eaif_request_id
    );

CREATE INDEX ix_gts_timetable_generation_result
    ON gts_timetable_generation_request (
        tenant_id,
        result_timetable_id
    );
