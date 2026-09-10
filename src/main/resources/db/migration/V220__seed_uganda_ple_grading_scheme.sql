-- GT-UGANDA-PLE-001
--
-- Uganda Primary Leaving Examination grading scheme.
--
-- Supports:
-- English
-- Mathematics
-- Science
-- Social Studies
--
-- Uses:
-- Marks
-- Grade points
-- Aggregation
-- Division classification
--
-- No grading logic is hard-coded.


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

    'UG-PLE',

    'Uganda Primary Leaving Examination Grading Scheme',

    'NUMERIC',

    TRUE,

    TRUE,

    TRUE,

    TRUE,

    'Uganda PLE grading framework supporting subject grades, grade points, aggregates and division classification.',

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

    AND gs.scheme_code = 'UG-PLE'

);
