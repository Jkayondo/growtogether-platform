CREATE TABLE gts_learner_progression_history (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    academic_year_id UUID,

    previous_class_grade_id UUID,

    new_class_grade_id UUID,

    promotion_decision_id UUID,

    progression_type VARCHAR(40) NOT NULL,

    progression_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',

    effective_date DATE NOT NULL,

    remarks VARCHAR(1000),

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',


    CONSTRAINT ck_gts_progression_history_type
        CHECK (
            progression_type IN (
                'PROMOTED',
                'REPEATED',
                'TRANSFERRED_IN',
                'TRANSFERRED_OUT',
                'WITHDRAWN',
                'GRADUATED'
            )
        ),


    CONSTRAINT ck_gts_progression_history_status
        CHECK (
            progression_status IN (
                'PENDING',
                'CONFIRMED',
                'CANCELLED',
                'ARCHIVED'
            )
        )

);


CREATE INDEX ix_gts_progression_history_learner
    ON gts_learner_progression_history(
        tenant_id,
        learner_id,
        effective_date
    );


CREATE INDEX ix_gts_progression_history_year
    ON gts_learner_progression_history(
        tenant_id,
        academic_year_id
    );


CREATE INDEX ix_gts_progression_history_decision
    ON gts_learner_progression_history(
        tenant_id,
        promotion_decision_id
    );
