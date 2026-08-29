-- GT School Release 1
-- A11.5 — Year-level timetable uniqueness hardening
--
-- V039 already protects term-scoped timetable versions and ACTIVE
-- timetable scope.
--
-- academic_term_id is nullable. PostgreSQL ordinary UNIQUE constraints
-- and indexes treat NULL values as distinct, so year-level timetables
-- require explicit partial unique indexes.

CREATE UNIQUE INDEX uq_gts_timetable_year_version
    ON gts_timetable (
        tenant_id,
        academic_year_id,
        campus_id,
        timetable_type,
        version_number
    )
    WHERE academic_term_id IS NULL;


CREATE UNIQUE INDEX uq_gts_active_timetable_year
    ON gts_timetable (
        tenant_id,
        academic_year_id,
        campus_id,
        timetable_type
    )
    WHERE academic_term_id IS NULL
      AND timetable_status = 'ACTIVE'
      AND status = 'ACTIVE';
