-- GT-UGANDA-UACE-CLASSIFICATION-001
--
-- Uganda Advanced Certificate of Education classification.
--
-- Total points determine final classification.
--
-- Classification logic is configuration driven.


INSERT INTO gts_grade_division_rule (

    id,
    tenant_id,
    grading_scheme_id,
    division_code,
    division_name,
    minimum_aggregate,
    maximum_aggregate,
    description,
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

    d.division_code,

    d.division_name,

    d.minimum_aggregate,

    d.maximum_aggregate,

    d.description,

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

    AND gs.scheme_code = 'UG-UACE'


CROSS JOIN (

    VALUES

    (
        'DIVISION_1',
        'Division 1',
        16,
        20,
        'Excellent Advanced Level performance',
        1
    ),

    (
        'DIVISION_2',
        'Division 2',
        11,
        15,
        'Very good Advanced Level performance',
        2
    ),

    (
        'DIVISION_3',
        'Division 3',
        6,
        10,
        'Good Advanced Level performance',
        3
    ),

    (
        'DIVISION_4',
        'Division 4',
        1,
        5,
        'Minimum pass classification',
        4
    ),

    (
        'DIVISION_U',
        'Ungraded',
        0,
        0,
        'Unsuccessful performance',
        5
    )

) AS d(

    division_code,
    division_name,
    minimum_aggregate,
    maximum_aggregate,
    description,
    sequence_number

)

WHERE NOT EXISTS (

    SELECT 1

    FROM gts_grade_division_rule dr

    WHERE dr.tenant_id = t.id

    AND dr.grading_scheme_id = gs.id

    AND dr.division_code = d.division_code

);
