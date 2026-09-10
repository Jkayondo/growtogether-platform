CREATE TABLE gts_promotion_rule (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    rule_code VARCHAR(80) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,

    description VARCHAR(1000),

    education_level_id UUID,

    from_class_grade_id UUID,
    to_class_grade_id UUID,

    minimum_average_score NUMERIC(8,2),
    minimum_subject_score NUMERIC(8,2),

    allow_conditional_promotion BOOLEAN NOT NULL DEFAULT FALSE,
    allow_repeat BOOLEAN NOT NULL DEFAULT TRUE,

    rule_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_promotion_rule_code
        UNIQUE (tenant_id, rule_code),

    CONSTRAINT ck_gts_promotion_rule_status
        CHECK (
            rule_status IN (
                'DRAFT',
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);
