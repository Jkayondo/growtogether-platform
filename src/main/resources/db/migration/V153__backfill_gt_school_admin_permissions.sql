-- GT-EIAM-R1-FIX-001B
-- Historical SCHOOL_ADMIN permission backfill.
--
-- Recovery established that migrations V121-V144 seeded permission
-- definitions but their intended SCHOOL_ADMIN assignments produced
-- zero rows because SCHOOL_ADMIN had not yet been bootstrapped.
--
-- V152 creates the missing role.
-- This migration restores ONLY the explicit permission assignments
-- originally authorised by V121-V144.
--
-- IMPORTANT:
-- This is deliberately an allow-list.
-- It does NOT grant every GT-SCHOOL permission to SCHOOL_ADMIN.

WITH intended_permission(code) AS (
    VALUES
        -- V121 — Academic year / term
        ('school.academic.year.create'),
        ('school.academic.year.read'),
        ('school.academic.term.create'),
        ('school.academic.term.read'),

        -- V122 — Curriculum
        ('school.academic.curriculum.create'),
        ('school.academic.curriculum.read'),
        ('school.academic.curriculum.manage'),

        -- V123 — Curriculum version
        ('school.academic.curriculum.version.create'),
        ('school.academic.curriculum.version.read'),
        ('school.academic.curriculum.version.manage'),

        -- V125 — Learning area
        ('school.academic.curriculum.learning-area.create'),
        ('school.academic.curriculum.learning-area.read'),
        ('school.academic.curriculum.learning-area.manage'),

        -- V127 — Subject
        ('school.academic.subject.create'),
        ('school.academic.subject.read'),
        ('school.academic.subject.manage'),

        -- V128 — Class / grade
        ('school.academic.class-grade.create'),
        ('school.academic.class-grade.read'),
        ('school.academic.class-grade.manage'),

        -- V129 — Campus
        ('school.academic.campus.create'),
        ('school.academic.campus.read'),
        ('school.academic.campus.manage'),

        -- V130 — Class offering
        ('school.academic.class-offering.create'),
        ('school.academic.class-offering.read'),
        ('school.academic.class-offering.manage'),

        -- V131 — Enterprise workforce
        ('enterprise.workforce.member.create'),
        ('enterprise.workforce.member.read'),
        ('enterprise.workforce.member.manage'),

        -- V132 — Teacher profile
        ('school.academic.teacher-profile.create'),
        ('school.academic.teacher-profile.read'),
        ('school.academic.teacher-profile.manage'),

        -- V133 — Teacher subject qualification
        ('school.academic.teacher-subject-qualification.create'),
        ('school.academic.teacher-subject-qualification.read'),
        ('school.academic.teacher-subject-qualification.manage'),

        -- V134 — Teaching assignment
        ('school.academic.teaching-assignment.create'),
        ('school.academic.teaching-assignment.read'),
        ('school.academic.teaching-assignment.manage'),

        -- V135 — Stream
        ('school.academic.stream.create'),
        ('school.academic.stream.read'),
        ('school.academic.stream.manage'),

        -- V136 — Student / enrollment
        ('school.student.create'),
        ('school.student.read'),
        ('school.student.manage'),
        ('school.enrollment.create'),
        ('school.enrollment.read'),
        ('school.enrollment.manage'),

        -- V137 — Guardian / guardian relationship
        ('school.guardian.create'),
        ('school.guardian.read'),
        ('school.guardian.manage'),
        ('school.guardian-relationship.create'),
        ('school.guardian-relationship.read'),
        ('school.guardian-relationship.manage'),

        -- V140 — Timetable
        ('school.timetable.create'),
        ('school.timetable.read'),
        ('school.timetable.manage'),
        ('school.timetable.review'),
        ('school.timetable.approve'),
        ('school.timetable.publish'),
        ('school.timetable.activate'),
        ('school.timetable.suspend'),

        -- V144 — Admission payment
        ('school.admission.payment.read'),
        ('school.admission.payment.manage'),
        ('school.admission.payment.waive'),
        ('school.admission.payment.reconcile')
)
INSERT INTO eiam_role_permission (
    id,
    tenant_id,
    role_id,
    permission_id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    t.id,
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM intended_permission intended
JOIN eiam_tenant t
    ON t.code = 'GT-SCHOOL'
JOIN eiam_role r
    ON r.tenant_id = t.id
   AND r.code = 'SCHOOL_ADMIN'
JOIN eiam_permission p
    ON p.tenant_id = t.id
   AND p.code = intended.code
ON CONFLICT DO NOTHING;
