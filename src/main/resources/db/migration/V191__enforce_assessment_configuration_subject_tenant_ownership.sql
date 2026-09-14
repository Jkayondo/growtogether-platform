-- GT-SCH-ASSESSMENT-CONFIG-PARENT-TENANT-INTEGRITY-001

ALTER TABLE subject_configurations
    ADD CONSTRAINT uq_subject_configurations_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE assessment_configurations
    ADD CONSTRAINT fk_assessment_configurations_subject_tenant
        FOREIGN KEY (tenant_id, subject_configuration_id)
        REFERENCES subject_configurations (tenant_id, id);

ALTER TABLE assessment_configurations
    ADD CONSTRAINT uq_assessment_configurations_tenant_subject_name
        UNIQUE (tenant_id, subject_configuration_id, assessment_name);
