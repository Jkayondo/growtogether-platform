-- GT-UGANDA-UCE-GRADES-001
--
-- Uganda Certificate of Education (UCE) grade boundaries.
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

    AND gs.scheme_code = 'UG-UCE'


CROSS JOIN (

    VALUES

    ('D1','Distinction One',80,100,1,TRUE,TRUE,'Exceptional performance',1),

    ('D2','Distinction Two',75,79,2,TRUE,TRUE,'Excellent performance',2),

    ('C3','Credit Three',70,74,3,TRUE,FALSE,'Very good performance',3),

    ('C4','Credit Four',65,69,4,TRUE,FALSE,'Good performance',4),

    ('C5','Credit Five',60,64,5,TRUE,FALSE,'Credit performance',5),

    ('C6','Credit Six',55,59,6,TRUE,FALSE,'Credit performance',6),

    ('P7','Pass Seven',50,54,7,TRUE,FALSE,'Pass performance',7),

    ('P8','Pass Eight',40,49,8,TRUE,FALSE,'Pass performance',8),

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
