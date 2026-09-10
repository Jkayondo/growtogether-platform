-- GT-UGANDA-PLE-AGGREGATION-001
--
-- Uganda Primary Leaving Examination aggregation rule.
--
-- Subject grade points are summed to produce aggregate score.
--
-- No aggregation logic is hard-coded.


INSERT INTO gts_grade_aggregation_rule (

    id,
    tenant_id,
    grading_scheme_id,
    rule_code,
    rule_name,
    aggregation_type,
    required_subject_count,
    best_subject_count,
    include_compulsory_subjects,
    uses_grade_points,
    description,
    status,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version

)

SELECT

    gen_random_uuid(),

    t.id,

    gs.id,

    'UG-PLE-SUBJECT-POINT-SUM',

    'Uganda PLE Subject Point Aggregation',

    'SUBJECT_POINT_SUM',

    4,

    4,

    TRUE,

    TRUE,

    'Aggregates Uganda PLE subject grade points from English, Mathematics, Science and Social Studies.',

    'ACTIVE',

    NOW(),

    'SYSTEM',

    NOW(),

    'SYSTEM',

    0

FROM eiam_tenant t

JOIN gts_grading_scheme gs

    ON gs.tenant_id = t.id

    AND gs.scheme_code = 'UG-PLE'


WHERE NOT EXISTS (

    SELECT 1

    FROM gts_grade_aggregation_rule gar

    WHERE gar.tenant_id = t.id

    AND gar.grading_scheme_id = gs.id

    AND gar.rule_code = 'UG-PLE-SUBJECT-POINT-SUM'

);
