-- Create GT School subject catalogue

CREATE TABLE gts_subject_catalogue (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    curriculum_version_id UUID NOT NULL,

    learning_area_id UUID NOT NULL,

    subject_code VARCHAR(50) NOT NULL,

    subject_name VARCHAR(150) NOT NULL,

    subject_type VARCHAR(50) NOT NULL,

    description VARCHAR(500),

    sequence_number INTEGER NOT NULL DEFAULT 1,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT fk_subject_catalogue_curriculum_version
        FOREIGN KEY (curriculum_version_id)
        REFERENCES gts_curriculum_version(id)
        ON DELETE CASCADE,


    CONSTRAINT fk_subject_catalogue_learning_area
        FOREIGN KEY (learning_area_id)
        REFERENCES gts_curriculum_learning_area(id)
        ON DELETE CASCADE,


    CONSTRAINT uq_subject_catalogue_code
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            subject_code
        )
);


CREATE INDEX ix_subject_catalogue_learning_area
    ON gts_subject_catalogue (
        tenant_id,
        learning_area_id
    );


CREATE INDEX ix_subject_catalogue_curriculum_version
    ON gts_subject_catalogue (
        tenant_id,
        curriculum_version_id
    );


CREATE INDEX ix_subject_catalogue_status
    ON gts_subject_catalogue (
        tenant_id,
        status
    );
