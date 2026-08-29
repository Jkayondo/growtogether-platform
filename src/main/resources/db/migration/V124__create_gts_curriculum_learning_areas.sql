-- Create GT School curriculum learning areas

CREATE TABLE gts_curriculum_learning_area (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    curriculum_version_id UUID NOT NULL,

    learning_area_code VARCHAR(50) NOT NULL,

    learning_area_name VARCHAR(150) NOT NULL,

    learning_area_type VARCHAR(50) NOT NULL,

    description VARCHAR(500),

    sequence_number INTEGER NOT NULL DEFAULT 1,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT fk_gts_learning_area_curriculum_version

        FOREIGN KEY (curriculum_version_id)

        REFERENCES gts_curriculum_version(id)

        ON DELETE CASCADE,


    CONSTRAINT uq_gts_learning_area_code

        UNIQUE (
            tenant_id,
            curriculum_version_id,
            learning_area_code
        )

);


CREATE INDEX ix_gts_learning_area_curriculum_version

ON gts_curriculum_learning_area (
    tenant_id,
    curriculum_version_id
);


CREATE INDEX ix_gts_learning_area_status

ON gts_curriculum_learning_area (
    tenant_id,
    status
);
