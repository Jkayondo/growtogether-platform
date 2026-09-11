-- GT School
-- Enforce tenant ownership across Student Enrollment parent relationships.
--
-- Application validation already enforces these boundaries.
-- This migration adds equivalent database-level protection.

-- Candidate keys required by composite foreign keys.

ALTER TABLE gts_student
    ADD CONSTRAINT uq_gts_student_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT uq_gts_student_enrollment_tenant_id
    UNIQUE (tenant_id, id);


-- Student must belong to the same tenant as the enrollment.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_student_tenant
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);


-- Academic Year must belong to the same tenant.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_academic_year_tenant
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);


-- Optional Academic Term must belong to the same tenant.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_academic_term_tenant
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);


-- Campus must belong to the same tenant.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_campus_tenant
    FOREIGN KEY (tenant_id, campus_id)
    REFERENCES gts_campus (tenant_id, id);


-- Class Grade must belong to the same tenant.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_class_grade_tenant
    FOREIGN KEY (tenant_id, class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);


-- Optional Stream must belong to the same tenant.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_stream_tenant
    FOREIGN KEY (tenant_id, stream_id)
    REFERENCES gts_stream (tenant_id, id);


-- Previous Enrollment, when present, must belong to the same tenant.

ALTER TABLE gts_student_enrollment
    ADD CONSTRAINT fk_gts_student_enrollment_previous_enrollment_tenant
    FOREIGN KEY (tenant_id, previous_enrollment_id)
    REFERENCES gts_student_enrollment (tenant_id, id);
