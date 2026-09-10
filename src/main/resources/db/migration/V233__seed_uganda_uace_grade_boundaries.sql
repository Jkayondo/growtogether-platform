-- GT-UGANDA-UACE-GRADES-001
--
-- Uganda Advanced Certificate of Education grade boundaries.
--
-- Converts subject marks into grades and grade points.
--
-- No grading logic is hard-coded.


INSERT INTO gts_grade_boundary (

    id,
    tenant_id,
    grading_scheme_id,
    grade_code,
    grade_name,
    minimum_score,
    maximum_score,
    grade_point,
    pass_grade,
    distinction_grade,
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

    g.grade_code,

    g.grade_name,

    g.minimum_score,

    g.maximum_score,

    g.grade_point,

    g.pass_grade,

    g.distinction_grade,

    g.description,

    g.sequence_number,

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

    ('A','Grade A',80,100,6,TRUE,TRUE,'Outstanding performance',1),

    ('B','Grade B',70,79,5,TRUE,FALSE,'Very good performance',2),

    ('C','Grade C',60,69,4,TRUE,FALSE,'Good performance',3),

    ('D','Grade D',50,59,3,TRUE,FALSE,'Average performance',4),

    ('E','Grade E',40,49,2,TRUE,FALSE,'Satisfactory performance',5),

    ('O','Ordinary Pass',30,39,1,TRUE,FALSE,'Minimum pass performance',6),

    ('F','Fail',0,29,0,FALSE,FALSE,'Below pass standard',7)

) AS g(

    grade_code,
    grade_name,
    minimum_score,
    maximum_score,
    grade_point,
    pass_grade,
    distinction_grade,
    description,
    sequence_number

)

WHERE NOT EXISTS (

    SELECT 1

    FROM gts_grade_boundary gb

    WHERE gb.tenant_id = t.id

    AND gb.grading_scheme_id = gs.id

    AND gb.grade_code = g.grade_code

);
