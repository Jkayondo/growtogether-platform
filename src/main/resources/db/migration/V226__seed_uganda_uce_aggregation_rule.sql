-- GT-UGANDA-UCE-AGGREGATION-001
--
-- Uganda Certificate of Education aggregation rule.
--
-- Subject grade points are used for final aggregate calculation.
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

    'UG-UCE-SUBJECT-POINT-SUM',

    'Uganda UCE Subject Point Aggregation',

    'SUBJECT_POINT_SUM',

    8,

    8,

    TRUE,

    TRUE,

    'Aggregates Uganda UCE subject grade points for Ordinary Level final classification.',

    'ACTIVE',

    NOW(),

    'SYSTEM',

    NOW(),

    'SYSTEM',

    0

FROM eiam_tenant t

JOIN gts_grading_scheme gs

    ON gs.tenant_id = t.id

    AND gs.scheme_code = 'UG-UCE'


WHERE NOT EXISTS (

    SELECT 1

    FROM gts_grade_aggregation_rule gar

    WHERE gar.tenant_id = t.id

    AND gar.grading_scheme_id = gs.id

    AND gar.rule_code = 'UG-UCE-SUBJECT-POINT-SUM'

);
