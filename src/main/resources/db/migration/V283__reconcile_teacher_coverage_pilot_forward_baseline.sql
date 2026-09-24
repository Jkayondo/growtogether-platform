-- GrowTogether School Teacher Curriculum Coverage
-- Forward-only migration reconciliation.
-- Historical V210/V270 files remain unchanged.
-- Replay is controlled by successful Flyway history,
-- not merely by physical-object existence.

DO $gtcov210$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM flyway_schema_history
        WHERE success = TRUE
          AND version = '210'
    ) THEN
        -- GT-TEACHER-COVERAGE-001
        --
        -- Teacher curriculum coverage tracking.
        -- Records teacher progress against curriculum expectations.
        -- Curriculum remains the source of truth.
        -- This table records actual delivery progress.

        CREATE TABLE gts_teacher_coverage (

            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

            tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),


            teacher_profile_id UUID NOT NULL,

            teaching_assignment_id UUID NOT NULL,


            academic_year_id UUID NOT NULL,

            academic_term_id UUID,


            curriculum_version_id UUID NOT NULL,

            curriculum_subject_id UUID,

            class_grade_id UUID NOT NULL,


            coverage_type VARCHAR(40) NOT NULL,

            coverage_item VARCHAR(500) NOT NULL,


            planned_week INTEGER,

            coverage_status VARCHAR(40) NOT NULL DEFAULT 'NOT_STARTED',


            completion_date DATE,

            teacher_remarks VARCHAR(1500),


            created_at TIMESTAMPTZ NOT NULL,

            created_by VARCHAR(150) NOT NULL,

            updated_at TIMESTAMPTZ NOT NULL,

            updated_by VARCHAR(150) NOT NULL,

            version BIGINT NOT NULL DEFAULT 0,

            status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',



            CONSTRAINT ck_teacher_coverage_type
                CHECK (
                    coverage_type IN (
                        'TOPIC',
                        'LEARNING_OUTCOME',
                        'COMPETENCY',
                        'THEME',
                        'ACTIVITY'
                    )
                ),


            CONSTRAINT ck_teacher_coverage_status
                CHECK (
                    coverage_status IN (
                        'NOT_STARTED',
                        'IN_PROGRESS',
                        'COMPLETED',
                        'REQUIRES_REMEDIATION',
                        'AHEAD_OF_SCHEDULE'
                    )
                )

        );



        CREATE INDEX ix_teacher_coverage_teacher
            ON gts_teacher_coverage(
                tenant_id,
                teacher_profile_id
            );


        CREATE INDEX ix_teacher_coverage_assignment
            ON gts_teacher_coverage(
                tenant_id,
                teaching_assignment_id
            );


        CREATE INDEX ix_teacher_coverage_curriculum
            ON gts_teacher_coverage(
                tenant_id,
                curriculum_version_id,
                class_grade_id
            );


        CREATE INDEX ix_teacher_coverage_term
            ON gts_teacher_coverage(
                tenant_id,
                academic_term_id
            );

    END IF;
END
$gtcov210$;

DO $gtcov270$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM flyway_schema_history
        WHERE success = TRUE
          AND version = '270'
    ) THEN
        -- GT Enterprise Platform
        -- V270 — Reconcile historical GT School TEACHER authorization baseline.
        --
        -- V269 established the intended minimum Teacher baseline:
        --
        --   school.academic.curriculum.read
        --   school.academic.class-grade.read
        --   school.academic.subject.read
        --   school.academic.teaching-assignment.read
        --   ai.request.create
        --   ai.request.read
        --   ai.runtime.execute
        --
        -- V269 created the three EAIF definitions tenant-wide but relied on earlier
        -- GT School migrations for the four academic definitions. Historical and
        -- test-created tenants demonstrate that those academic definitions are not
        -- present tenant-wide.
        --
        -- V270 therefore reconciles the intended baseline for historical tenants.
        --
        -- Governance:
        --   * preserve an existing TEACHER role when present;
        --   * create only missing canonical permission definitions;
        --   * grant only the seven baseline authorities to TEACHER;
        --   * assign no users to TEACHER;
        --   * grant no authority to administrator roles;
        --   * configure no provider, model, connector, credential or runtime switch.

        INSERT INTO eiam_role (
            id,
            tenant_id,
            code,
            name,
            description,
            system_role,
            created_at,
            created_by,
            updated_at,
            updated_by,
            version,
            status
        )
        SELECT
            gen_random_uuid(),
            tenant.id,
            'TEACHER',
            'Teacher',
            'GT School teacher role for governed teacher-facing capabilities.',
            FALSE,
            CURRENT_TIMESTAMP,
            'GT-MIGRATION-V270',
            CURRENT_TIMESTAMP,
            'GT-MIGRATION-V270',
            0,
            'ACTIVE'
        FROM eiam_tenant tenant
        WHERE NOT EXISTS (
            SELECT 1
            FROM eiam_role existing
            WHERE existing.tenant_id = tenant.id
              AND existing.code = 'TEACHER'
        );

        INSERT INTO eiam_permission (
            id,
            tenant_id,
            code,
            name,
            module,
            description,
            system_permission,
            created_at,
            created_by,
            updated_at,
            updated_by,
            version,
            status
        )
        SELECT
            gen_random_uuid(),
            tenant.id,
            definition.code,
            definition.name,
            definition.module,
            definition.description,
            definition.system_permission,
            CURRENT_TIMESTAMP,
            'GT-MIGRATION-V270',
            CURRENT_TIMESTAMP,
            'GT-MIGRATION-V270',
            0,
            'ACTIVE'
        FROM eiam_tenant tenant
        CROSS JOIN (
            VALUES
                (
                    'school.academic.curriculum.read',
                    'Read Curriculum',
                    'SCHOOL_ACADEMIC',
                    'Allows viewing curricula',
                    FALSE
                ),
                (
                    'school.academic.class-grade.read',
                    'Read Class Grades',
                    'SCHOOL_ACADEMIC',
                    'Allows viewing academic class grades',
                    FALSE
                ),
                (
                    'school.academic.subject.read',
                    'Read Subjects',
                    'SCHOOL_ACADEMIC',
                    'Allows viewing academic subjects',
                    FALSE
                ),
                (
                    'school.academic.teaching-assignment.read',
                    'Read Teaching Assignments',
                    'SCHOOL_ACADEMIC',
                    'Allows viewing teacher academic assignments',
                    FALSE
                ),
                (
                    'ai.request.create',
                    'AI Request Create',
                    'EAIF',
                    'Create governed enterprise AI requests.',
                    TRUE
                ),
                (
                    'ai.request.read',
                    'AI Request Read',
                    'EAIF',
                    'Read governed enterprise AI request state and results.',
                    TRUE
                ),
                (
                    'ai.runtime.execute',
                    'AI Runtime Execute',
                    'EAIF',
                    'Execute an authorised governed enterprise AI request.',
                    TRUE
                )
        ) AS definition (
            code,
            name,
            module,
            description,
            system_permission
        )
        WHERE NOT EXISTS (
            SELECT 1
            FROM eiam_permission existing
            WHERE existing.tenant_id = tenant.id
              AND existing.code = definition.code
        );

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
            role.tenant_id,
            role.id,
            permission.id,
            CURRENT_TIMESTAMP,
            'GT-MIGRATION-V270',
            CURRENT_TIMESTAMP,
            'GT-MIGRATION-V270',
            0,
            'ACTIVE'
        FROM eiam_role role
        JOIN eiam_permission permission
          ON permission.tenant_id = role.tenant_id
        WHERE role.code = 'TEACHER'
          AND role.status = 'ACTIVE'
          AND permission.status = 'ACTIVE'
          AND permission.code IN (
              'school.academic.curriculum.read',
              'school.academic.class-grade.read',
              'school.academic.subject.read',
              'school.academic.teaching-assignment.read',
              'ai.request.create',
              'ai.request.read',
              'ai.runtime.execute'
          )
          AND NOT EXISTS (
              SELECT 1
              FROM eiam_role_permission existing
              WHERE existing.tenant_id = role.tenant_id
                AND existing.role_id = role.id
                AND existing.permission_id = permission.id
          );
    END IF;
END
$gtcov270$;
