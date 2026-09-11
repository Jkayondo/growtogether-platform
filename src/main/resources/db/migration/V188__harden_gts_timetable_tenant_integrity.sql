-- GT-SCH-TIMETABLE-DB-TENANT-INTEGRITY-001
-- GT-SCH-TIMETABLE-CLASS-SLOT-NULL-UNIQUENESS-001
--
-- Hardens GT School Timetable database integrity.
--
-- Application services already enforce tenant ownership and timetable
-- relationship rules. This migration adds equivalent database-level
-- tenant protection as defence in depth.
--
-- Existing single-column foreign keys are intentionally retained.
--
-- It also closes the PostgreSQL NULL uniqueness gap for class-wide
-- timetable slots where stream_id IS NULL.


-- ============================================================
-- Composite parent candidate keys
-- ============================================================

ALTER TABLE gts_bell_schedule
    ADD CONSTRAINT uq_gts_bell_schedule_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_bell_period
    ADD CONSTRAINT uq_gts_bell_period_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_scheduling_resource
    ADD CONSTRAINT uq_gts_scheduling_resource_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_subject_offering
    ADD CONSTRAINT uq_gts_subject_offering_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT uq_gts_teaching_assignment_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_teacher_leave_extension
    ADD CONSTRAINT uq_gts_teacher_leave_extension_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable
    ADD CONSTRAINT uq_gts_timetable_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT uq_gts_timetable_entry_tenant_id
    UNIQUE (
        tenant_id,
        id
    );


-- ============================================================
-- Bell Schedule -> Campus
-- ============================================================

ALTER TABLE gts_bell_schedule
    ADD CONSTRAINT fk_gts_bell_schedule_campus_tenant
    FOREIGN KEY (
        tenant_id,
        campus_id
    )
    REFERENCES gts_campus (
        tenant_id,
        id
    );


-- ============================================================
-- Bell Period -> Bell Schedule
-- ============================================================

ALTER TABLE gts_bell_period
    ADD CONSTRAINT fk_gts_bell_period_schedule_tenant
    FOREIGN KEY (
        tenant_id,
        bell_schedule_id
    )
    REFERENCES gts_bell_schedule (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


-- ============================================================
-- Scheduling Resource parents
-- ============================================================

ALTER TABLE gts_scheduling_resource
    ADD CONSTRAINT fk_gts_sched_resource_campus_tenant
    FOREIGN KEY (
        tenant_id,
        campus_id
    )
    REFERENCES gts_campus (
        tenant_id,
        id
    );


ALTER TABLE gts_scheduling_resource
    ADD CONSTRAINT fk_gts_sched_resource_subject_tenant
    FOREIGN KEY (
        tenant_id,
        specialized_for_subject_id
    )
    REFERENCES gts_subject (
        tenant_id,
        id
    );


-- ============================================================
-- Resource Unavailability -> Scheduling Resource
-- ============================================================

ALTER TABLE gts_resource_unavailability
    ADD CONSTRAINT fk_gts_resource_unavailability_resource_tenant
    FOREIGN KEY (
        tenant_id,
        scheduling_resource_id
    )
    REFERENCES gts_scheduling_resource (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


-- ============================================================
-- Teacher Unavailability parents
-- ============================================================

ALTER TABLE gts_teacher_unavailability
    ADD CONSTRAINT fk_gts_teacher_unavailability_teacher_tenant
    FOREIGN KEY (
        tenant_id,
        teacher_profile_id
    )
    REFERENCES gts_teacher_profile (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


ALTER TABLE gts_teacher_unavailability
    ADD CONSTRAINT fk_gts_teacher_unavailability_leave_tenant
    FOREIGN KEY (
        tenant_id,
        leave_extension_id
    )
    REFERENCES gts_teacher_leave_extension (
        tenant_id,
        id
    );


-- ============================================================
-- Timetable parents
-- ============================================================

ALTER TABLE gts_timetable
    ADD CONSTRAINT fk_gts_timetable_year_tenant
    FOREIGN KEY (
        tenant_id,
        academic_year_id
    )
    REFERENCES gts_academic_year (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable
    ADD CONSTRAINT fk_gts_timetable_term_tenant
    FOREIGN KEY (
        tenant_id,
        academic_term_id
    )
    REFERENCES gts_academic_term (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable
    ADD CONSTRAINT fk_gts_timetable_campus_tenant
    FOREIGN KEY (
        tenant_id,
        campus_id
    )
    REFERENCES gts_campus (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable
    ADD CONSTRAINT fk_gts_timetable_bell_schedule_tenant
    FOREIGN KEY (
        tenant_id,
        bell_schedule_id
    )
    REFERENCES gts_bell_schedule (
        tenant_id,
        id
    );


-- ============================================================
-- Timetable Entry parents
-- ============================================================

ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_timetable_tenant
    FOREIGN KEY (
        tenant_id,
        timetable_id
    )
    REFERENCES gts_timetable (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_bell_period_tenant
    FOREIGN KEY (
        tenant_id,
        bell_period_id
    )
    REFERENCES gts_bell_period (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_class_grade_tenant
    FOREIGN KEY (
        tenant_id,
        class_grade_id
    )
    REFERENCES gts_class_grade (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_class_offering_tenant
    FOREIGN KEY (
        tenant_id,
        class_offering_id
    )
    REFERENCES gts_class_offering (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_subject_offering_tenant
    FOREIGN KEY (
        tenant_id,
        subject_offering_id
    )
    REFERENCES gts_subject_offering (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_stream_tenant
    FOREIGN KEY (
        tenant_id,
        stream_id
    )
    REFERENCES gts_stream (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_assignment_tenant
    FOREIGN KEY (
        tenant_id,
        teaching_assignment_id
    )
    REFERENCES gts_teaching_assignment (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_teacher_tenant
    FOREIGN KEY (
        tenant_id,
        teacher_profile_id
    )
    REFERENCES gts_teacher_profile (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_entry
    ADD CONSTRAINT fk_gts_entry_resource_tenant
    FOREIGN KEY (
        tenant_id,
        scheduling_resource_id
    )
    REFERENCES gts_scheduling_resource (
        tenant_id,
        id
    );


-- ============================================================
-- Timetable Conflict parents
-- ============================================================

ALTER TABLE gts_timetable_conflict
    ADD CONSTRAINT fk_gts_conflict_timetable_tenant
    FOREIGN KEY (
        tenant_id,
        timetable_id
    )
    REFERENCES gts_timetable (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


ALTER TABLE gts_timetable_conflict
    ADD CONSTRAINT fk_gts_conflict_entry_tenant
    FOREIGN KEY (
        tenant_id,
        timetable_entry_id
    )
    REFERENCES gts_timetable_entry (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


ALTER TABLE gts_timetable_conflict
    ADD CONSTRAINT fk_gts_conflict_other_entry_tenant
    FOREIGN KEY (
        tenant_id,
        conflicting_entry_id
    )
    REFERENCES gts_timetable_entry (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


-- ============================================================
-- Timetable Change History parents
-- ============================================================

ALTER TABLE gts_timetable_change_history
    ADD CONSTRAINT fk_gts_history_timetable_tenant
    FOREIGN KEY (
        tenant_id,
        timetable_id
    )
    REFERENCES gts_timetable (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


ALTER TABLE gts_timetable_change_history
    ADD CONSTRAINT fk_gts_history_entry_tenant
    FOREIGN KEY (
        tenant_id,
        timetable_entry_id
    )
    REFERENCES gts_timetable_entry (
        tenant_id,
        id
    )
    ON DELETE CASCADE;


-- ============================================================
-- Timetable Generation Request tenant + parents
-- ============================================================

ALTER TABLE gts_timetable_generation_request
    ADD CONSTRAINT fk_gts_generation_tenant
    FOREIGN KEY (
        tenant_id
    )
    REFERENCES eiam_tenant (
        id
    );


ALTER TABLE gts_timetable_generation_request
    ADD CONSTRAINT fk_gts_generation_year_tenant
    FOREIGN KEY (
        tenant_id,
        academic_year_id
    )
    REFERENCES gts_academic_year (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_generation_request
    ADD CONSTRAINT fk_gts_generation_term_tenant
    FOREIGN KEY (
        tenant_id,
        academic_term_id
    )
    REFERENCES gts_academic_term (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_generation_request
    ADD CONSTRAINT fk_gts_generation_campus_tenant
    FOREIGN KEY (
        tenant_id,
        campus_id
    )
    REFERENCES gts_campus (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_generation_request
    ADD CONSTRAINT fk_gts_generation_bell_schedule_tenant
    FOREIGN KEY (
        tenant_id,
        bell_schedule_id
    )
    REFERENCES gts_bell_schedule (
        tenant_id,
        id
    );


ALTER TABLE gts_timetable_generation_request
    ADD CONSTRAINT fk_gts_generation_result_timetable_tenant
    FOREIGN KEY (
        tenant_id,
        result_timetable_id
    )
    REFERENCES gts_timetable (
        tenant_id,
        id
    );


-- ============================================================
-- NULL-safe class-slot uniqueness
-- ============================================================
--
-- stream_id is optional. PostgreSQL ordinary UNIQUE indexes treat
-- NULL values as distinct, so the former partial unique index could
-- admit multiple active class-wide entries into the same slot.
--
-- PostgreSQL 15+ NULLS NOT DISTINCT gives NULL stream scope the
-- required class-wide uniqueness semantics while preserving distinct
-- non-NULL streams.

DROP INDEX uq_gts_timetable_class_slot;

CREATE UNIQUE INDEX uq_gts_timetable_class_slot
    ON gts_timetable_entry (
        tenant_id,
        timetable_id,
        day_of_week,
        bell_period_id,
        class_grade_id,
        stream_id
    )
    NULLS NOT DISTINCT
    WHERE (
        entry_status IN (
            'SCHEDULED',
            'CONFIRMED',
            'ACTIVE'
        )
        AND status = 'ACTIVE'
        AND class_grade_id IS NOT NULL
    );
