ALTER TABLE gts_learning_outcome
    ADD CONSTRAINT uq_gts_learning_outcome_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE gts_assessment
    ADD CONSTRAINT fk_gts_assessment_learning_outcome_tenant
        FOREIGN KEY (tenant_id, learning_outcome_id)
        REFERENCES gts_learning_outcome (tenant_id, id);
