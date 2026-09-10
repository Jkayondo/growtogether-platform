-- GT-UGANDA-PLE-DIVISION-001
--
-- Uganda Primary Leaving Examination division classification.
--
-- Aggregate score determines final division.
--
-- No division logic is hard-coded.


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

    AND gs.scheme_code = 'UG-PLE'


CROSS JOIN (

    VALUES

    (
        'DIVISION_1',
        'Division 1',
        4,
        12,
        'Excellent overall performance',
        1
    ),

    (
        'DIVISION_2',
        'Division 2',
        13,
        23,
        'Very good overall performance',
        2
    ),

    (
        'DIVISION_3',
        'Division 3',
        24,
        29,
        'Good overall performance',
        3
    ),

    (
        'DIVISION_4',
        'Division 4',
        30,
        34,
        'Pass level performance',
        4
    ),

    (
        'DIVISION_U',
        'Division U',
        35,
        36,
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
