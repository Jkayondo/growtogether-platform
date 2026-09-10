-- GT-UGANDA-PRIMARY-LOWER-001
--
-- Uganda Lower Primary competency grading scheme.
--
-- Supports:
-- Primary One
-- Primary Two
-- Primary Three
--
-- Competency based assessment.
-- No numerical marks.


INSERT INTO gts_grading_scheme (

    id,
    tenant_id,
    scheme_code,
    scheme_name,
    grade_scale_type,
    uses_marks,
    uses_comments,
    uses_grade_points,
    uses_aggregation,
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

    'UG-PRIMARY-LOWER-COMPETENCY',

    'Uganda Lower Primary Competency Assessment',

    'COMPETENCY',

    FALSE,

    TRUE,

    FALSE,

    FALSE,

    'Competency based assessment framework for Uganda Lower Primary learners in Primary One, Primary Two and Primary Three.',

    'ACTIVE',

    NOW(),

    'SYSTEM',

    NOW(),

    'SYSTEM',

    0

FROM eiam_tenant t

WHERE NOT EXISTS (

    SELECT 1

    FROM gts_grading_scheme gs

    WHERE gs.tenant_id = t.id

    AND gs.scheme_code = 'UG-PRIMARY-LOWER-COMPETENCY'

);
