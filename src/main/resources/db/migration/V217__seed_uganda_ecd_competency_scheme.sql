-- GT-UGANDA-ECD-001
--
-- Uganda Early Childhood Development competency grading scheme.
--
-- Supports:
-- Baby Class
-- Middle Class
-- Top Class
--
-- No numerical marks.
-- Uses competency descriptors and teacher comments.


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

    'UG-ECD-COMPETENCY',

    'Uganda ECD Competency Assessment',

    'COMPETENCY',

    FALSE,

    TRUE,

    FALSE,

    FALSE,

    'Competency based assessment framework for Uganda Early Childhood Development levels including Baby Class, Middle Class and Top Class.',

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

    AND gs.scheme_code = 'UG-ECD-COMPETENCY'

);
