CREATE TABLE gts_promotion_decision (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    academic_year_id UUID,

    current_class_grade_id UUID,

    promoted_class_grade_id UUID,

    promotion_rule_id UUID
        REFERENCES gts_promotion_rule(id),

    decision_status VARCHAR(40) NOT NULL DEFAULT 'PENDING_REVIEW',

    decision_reason VARCHAR(1000),

    reviewed_by VARCHAR(150),

    reviewed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT ck_gts_promotion_decision_status
        CHECK (
            decision_status IN (
                'PENDING_REVIEW',
                'RECOMMENDED',
                'APPROVED',
                'REJECTED',
                'PROMOTED',
                'REPEATED',
                'CONDITIONAL_PROMOTION'
            )
        )

);


CREATE INDEX ix_gts_promotion_decision_tenant
    ON gts_promotion_decision(tenant_id);


CREATE INDEX ix_gts_promotion_decision_learner
    ON gts_promotion_decision(learner_id);
