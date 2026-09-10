-- GT-UGANDA-UCE-001
--
-- Uganda Ordinary Level (UCE) grading scheme.
--
-- Supports:
-- Senior One to Senior Four
-- Uganda Certificate of Education
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

    'UG-UCE',

    'Uganda Ordinary Level Education Certificate Grading Scheme',

    'NUMERIC',

    TRUE,

    TRUE,

    TRUE,

    TRUE,

    'Uganda UCE grading framework supporting subject grades, grade points, aggregation and division classification for Ordinary Level learners.',

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

    AND gs.scheme_code = 'UG-UCE'

);
