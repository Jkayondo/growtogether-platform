-- GT-UGANDA-UACE-AGGREGATION-001
--
-- Uganda Advanced Level aggregation rule.
--
-- Supports principal and subsidiary subject points.
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

    'UG-UACE-PRINCIPAL-SUBSIDIARY-POINTS',

    'Uganda UACE Principal and Subsidiary Subject Points Aggregation',

    'PRINCIPAL_SUBSIDIARY_POINTS',

    3,

    3,

    TRUE,

    TRUE,

    'Aggregates UACE principal subject points and subsidiary subject contribution for Advanced Level final results.',

    'ACTIVE',

    NOW(),

    'SYSTEM',

    NOW(),

    'SYSTEM',

    0

FROM eiam_tenant t

JOIN gts_grading_scheme gs

    ON gs.tenant_id = t.id

    AND gs.scheme_code = 'UG-UACE'


WHERE NOT EXISTS (

    SELECT 1

    FROM gts_grade_aggregation_rule gar

    WHERE gar.tenant_id = t.id

    AND gar.grading_scheme_id = gs.id

    AND gar.rule_code = 'UG-UACE-PRINCIPAL-SUBSIDIARY-POINTS'

);
