-- GT-UGANDA-UCE-DIVISION-001
--
-- Uganda Certificate of Education division classification.
--
-- Aggregate score determines final division.
--
-- Division logic is configuration driven.


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

    AND gs.scheme_code = 'UG-UCE'


CROSS JOIN (

    VALUES

    (
        'DIVISION_1',
        'Division 1',
        8,
        32,
        'Excellent overall UCE performance',
        1
    ),

    (
        'DIVISION_2',
        'Division 2',
        33,
        56,
        'Very good overall UCE performance',
        2
    ),

    (
        'DIVISION_3',
        'Division 3',
        57,
        80,
        'Good overall UCE performance',
        3
    ),

    (
        'DIVISION_4',
        'Division 4',
        81,
        100,
        'Pass level UCE performance',
        4
    ),

    (
        'DIVISION_U',
        'Division U',
        101,
        999,
        'Ungraded or unsuccessful performance',
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
