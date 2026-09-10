-- GT-UGANDA-PLE-GRADES-001
--
-- Uganda Primary Leaving Examination grade boundaries.
--
-- Converts marks into grades and grade points.
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

    AND gs.scheme_code = 'UG-PLE'


CROSS JOIN (

    VALUES

    ('D1','Distinction One',85,100,1,TRUE,TRUE,'Excellent performance',1),

    ('D2','Distinction Two',75,84,2,TRUE,TRUE,'Very good performance',2),

    ('C3','Credit Three',65,74,3,TRUE,FALSE,'Good performance',3),

    ('C4','Credit Four',60,64,4,TRUE,FALSE,'Good performance',4),

    ('C5','Credit Five',55,59,5,TRUE,FALSE,'Credit performance',5),

    ('C6','Credit Six',50,54,6,TRUE,FALSE,'Credit performance',6),

    ('C7','Credit Seven',45,49,7,TRUE,FALSE,'Pass credit performance',7),

    ('P8','Pass Eight',40,44,8,TRUE,FALSE,'Pass performance',8),

    ('F9','Fail Nine',0,39,9,FALSE,FALSE,'Below pass standard',9)

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
