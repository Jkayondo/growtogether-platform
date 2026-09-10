-- GT-SCH-ASSESSMENT-PLAN-SCOPE-UNIQUENESS-001
--
-- PostgreSQL ordinary UNIQUE constraints treat NULL values as distinct.
-- Academic term, academic programme, study track and stream are optional
-- within an assessment-plan academic scope, therefore the original
-- uq_gts_assessment_plan_scope constraint could admit duplicate plans
-- whenever one or more of those scope values was NULL.
--
-- PostgreSQL 15+ UNIQUE NULLS NOT DISTINCT gives the intended scope
-- semantics without introducing sentinel UUID values.

ALTER TABLE gts_assessment_plan
    DROP CONSTRAINT uq_gts_assessment_plan_scope;

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT uq_gts_assessment_plan_scope
    UNIQUE NULLS NOT DISTINCT (
        tenant_id,
        academic_year_id,
        academic_term_id,
        campus_id,
        academic_programme_id,
        study_track_id,
        class_grade_id,
        stream_id
    );
