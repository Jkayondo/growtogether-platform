-- GT-UGANDA-UACE-001
--
-- Uganda Advanced Level Education grading scheme.
--
-- Supports:
-- Senior Five and Senior Six
-- Uganda Advanced Certificate of Education
--
-- Uses:
-- Marks
-- Grade points
-- Aggregation
-- Subject classification
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

    'UG-UACE',

    'Uganda Advanced Level Education Certificate Grading Scheme',

    'NUMERIC',

    TRUE,

    TRUE,

    TRUE,

    TRUE,

    'Uganda UACE grading framework supporting principal subjects, subsidiary subjects, grade points, subject combinations and final point aggregation.',

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

    AND gs.scheme_code = 'UG-UACE'

);
