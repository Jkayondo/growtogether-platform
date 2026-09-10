-- GT-UGANDA-ECD-DESCRIPTORS-001
--
-- Uganda Early Childhood Development competency descriptors.
--
-- No marks are used.
-- Performance is reported through competency levels and comments.


INSERT INTO gts_competency_descriptor (

    id,
    tenant_id,
    grading_scheme_id,
    descriptor_code,
    descriptor_name,
    performance_level,
    description,
    teacher_guidance,
    sequence_number,
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

    d.descriptor_code,

    d.descriptor_name,

    d.descriptor_code,

    d.description,

    d.teacher_guidance,

    d.sequence_number,

    'ACTIVE',

    NOW(),

    'SYSTEM',

    NOW(),

    'SYSTEM',

    0

FROM eiam_tenant t

JOIN gts_grading_scheme gs

    ON gs.tenant_id = t.id

    AND gs.scheme_code = 'UG-ECD-COMPETENCY'


CROSS JOIN (

    VALUES

    (
        'EMERGING',
        'Emerging',
        'Beginning to demonstrate the expected competency with significant support.',
        'Provide guided activities, modelling and continuous encouragement.',
        1
    ),

    (
        'DEVELOPING',
        'Developing',
        'Demonstrates the competency with occasional support and guidance.',
        'Provide practice opportunities and targeted support.',
        2
    ),

    (
        'ACHIEVED',
        'Achieved',
        'Demonstrates the expected competency independently.',
        'Continue enrichment activities and extension opportunities.',
        3
    ),

    (
        'EXCEEDING',
        'Exceeding Expectations',
        'Demonstrates advanced understanding and application of the competency.',
        'Provide challenging activities to further develop potential.',
        4
    )

) AS d(

    descriptor_code,
    descriptor_name,
    description,
    teacher_guidance,
    sequence_number

)

WHERE NOT EXISTS (

    SELECT 1

    FROM gts_competency_descriptor cd

    WHERE cd.tenant_id = t.id

    AND cd.grading_scheme_id = gs.id

    AND cd.descriptor_code = d.descriptor_code

);
