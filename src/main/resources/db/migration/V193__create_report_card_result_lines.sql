CREATE TABLE report_card_result_lines (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    report_card_id UUID NOT NULL
        REFERENCES report_cards(id)
        ON DELETE CASCADE,

    student_subject_result_id UUID NOT NULL
        REFERENCES gts_student_subject_result(id),

    subject_name VARCHAR(200) NOT NULL,

    final_score NUMERIC(10,2),

    grade_code VARCHAR(40),

    grade_name VARCHAR(120),

    grade_point NUMERIC(8,2),

    teacher_comment VARCHAR(2000),

    sequence_number INTEGER NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_report_card_result_line
        UNIQUE (
            tenant_id,
            report_card_id,
            student_subject_result_id
        ),


    CONSTRAINT ck_report_card_result_sequence
        CHECK(sequence_number > 0)

);
