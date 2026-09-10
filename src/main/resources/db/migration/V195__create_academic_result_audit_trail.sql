CREATE TABLE gts_academic_result_audit_event (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID
        REFERENCES gts_student(id),

    student_subject_result_id UUID
        REFERENCES gts_student_subject_result(id),

    report_card_id UUID
        REFERENCES report_cards(id),

    event_type VARCHAR(50) NOT NULL,

    previous_value JSONB,

    new_value JSONB,

    event_reason VARCHAR(1000),

    performed_by UUID NOT NULL,

    performed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT ck_gts_result_audit_event_type
    CHECK (
        event_type IN (
            'RESULT_CREATED',
            'SCORE_ENTERED',
            'SCORE_UPDATED',
            'GRADE_CALCULATED',
            'RESULT_VALIDATED',
            'RESULT_APPROVED',
            'RESULT_PUBLISHED',
            'RESULT_UNPUBLISHED',
            'RESULT_VIEWED'
        )
    )

);
