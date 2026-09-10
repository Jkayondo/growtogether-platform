-- GT-UGANDA-GRADING-001
--
-- Uganda Education System grading profile.
--
-- Parent profile for:
-- ECD competency
-- Lower Primary competency
-- PLE grading
-- UCE grading
-- UACE grading


INSERT INTO gts_education_grading_profile (

    id,
    tenant_id,
    profile_code,
    profile_name,
    country_code,
    education_system,
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

    'UGANDA-EDUCATION',

    'Uganda National Education System',

    'UG',

    'Uganda National Curriculum',

    'Uganda education grading framework supporting competency and examination based assessment from early childhood to advanced level.',

    'ACTIVE',

    NOW(),

    'SYSTEM',

    NOW(),

    'SYSTEM',

    0

FROM eiam_tenant t

WHERE NOT EXISTS (

    SELECT 1

    FROM gts_education_grading_profile p

    WHERE p.tenant_id = t.id

    AND p.profile_code = 'UGANDA-EDUCATION'

);
