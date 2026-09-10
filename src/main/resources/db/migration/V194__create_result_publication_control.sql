CREATE TABLE gts_result_publication (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    publication_type VARCHAR(40) NOT NULL,

    published_at TIMESTAMPTZ,

    published_by UUID,

    unpublished_at TIMESTAMPTZ,

    unpublished_by UUID,

    publication_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    publication_message VARCHAR(1000),


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT ck_gts_result_publication_type
    CHECK (
        publication_type IN (
            'TERM_RESULT',
            'EXAMINATION_RESULT',
            'REPORT_CARD',
            'SUBJECT_RESULT'
        )
    ),


    CONSTRAINT ck_gts_result_publication_status
    CHECK (
        publication_status IN (
            'DRAFT',
            'APPROVED',
            'PUBLISHED',
            'UNPUBLISHED'
        )
    )

);
