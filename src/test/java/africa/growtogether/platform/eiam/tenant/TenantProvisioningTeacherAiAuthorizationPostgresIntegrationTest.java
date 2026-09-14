package africa.growtogether.platform.eiam.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class TenantProvisioningTeacherAiAuthorizationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName(
                            "growtogether_tenant_teacher_ai_provisioning_test"
                    )
                    .withUsername("growtogether")
                    .withPassword("growtogether");

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );
        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private TenantProvisioningService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void newlyProvisionedTenantReceivesTeacherAiBaseline() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-TEACHER-AI-TEST-ORG",
                                "GT Teacher AI Test Organisation",
                                "GT-TEACHER-AI-TEST",
                                "GT Teacher AI Test Tenant",
                                "teacher-ai-admin@growtogether.africa",
                                "teacher-ai-admin",
                                "Strong-Test-Password-2026!",
                                "Teacher AI Tenant Administrator"
                        )
                );

        Integer teacherRoles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'TEACHER'
                          AND system_role = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(teacherRoles)
                .isEqualTo(1);

        Integer baselinePermissions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code IN (
                            'school.academic.curriculum.read',
                            'school.academic.class-grade.read',
                            'school.academic.subject.read',
                            'school.academic.teaching-assignment.read',
                            'ai.request.create',
                            'ai.request.read',
                            'ai.runtime.execute'
                          )
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(baselinePermissions)
                .isEqualTo(7);

        Integer canonicalAiPermissions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND (
                            (
                              code = 'ai.request.create'
                              AND name = 'AI Request Create'
                              AND module = 'EAIF'
                              AND description = 'Create governed enterprise AI requests.'
                              AND system_permission = TRUE
                            )
                            OR (
                              code = 'ai.request.read'
                              AND name = 'AI Request Read'
                              AND module = 'EAIF'
                              AND description = 'Read governed enterprise AI request state and results.'
                              AND system_permission = TRUE
                            )
                            OR (
                              code = 'ai.runtime.execute'
                              AND name = 'AI Runtime Execute'
                              AND module = 'EAIF'
                              AND description = 'Execute an authorised governed enterprise AI request.'
                              AND system_permission = TRUE
                            )
                          )
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(canonicalAiPermissions)
                .isEqualTo(3);

        Integer canonicalSchoolPermissions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND (
                            (
                              code = 'school.academic.curriculum.read'
                              AND name = 'Read Curriculum'
                              AND module = 'SCHOOL_ACADEMIC'
                              AND description = 'Allows viewing curricula'
                              AND system_permission = FALSE
                            )
                            OR (
                              code = 'school.academic.class-grade.read'
                              AND name = 'Read Class Grades'
                              AND module = 'SCHOOL_ACADEMIC'
                              AND description = 'Allows viewing academic class grades'
                              AND system_permission = FALSE
                            )
                            OR (
                              code = 'school.academic.subject.read'
                              AND name = 'Read Subjects'
                              AND module = 'SCHOOL_ACADEMIC'
                              AND description = 'Allows viewing academic subjects'
                              AND system_permission = FALSE
                            )
                            OR (
                              code = 'school.academic.teaching-assignment.read'
                              AND name = 'Read Teaching Assignments'
                              AND module = 'SCHOOL_ACADEMIC'
                              AND description = 'Allows viewing teacher academic assignments'
                              AND system_permission = FALSE
                            )
                          )
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(canonicalSchoolPermissions)
                .isEqualTo(4);

        Integer teacherGrants =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = rp.tenant_id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = rp.tenant_id
                        WHERE rp.tenant_id = ?
                          AND r.code = 'TEACHER'
                          AND p.code IN (
                            'school.academic.curriculum.read',
                            'school.academic.class-grade.read',
                            'school.academic.subject.read',
                            'school.academic.teaching-assignment.read',
                            'ai.request.create',
                            'ai.request.read',
                            'ai.runtime.execute'
                          )
                          AND rp.status = 'ACTIVE'
                          AND p.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(teacherGrants)
                .isEqualTo(7);

        Integer administrativeLeakage =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = rp.tenant_id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = rp.tenant_id
                        WHERE rp.tenant_id = ?
                          AND r.code IN (
                            'TENANT_ADMIN',
                            'INTEGRATION_ADMIN',
                            'SCHOOL_ADMIN'
                          )
                          AND p.code IN (
                            'school.academic.curriculum.read',
                            'school.academic.class-grade.read',
                            'school.academic.subject.read',
                            'school.academic.teaching-assignment.read',
                            'ai.request.create',
                            'ai.request.read',
                            'ai.runtime.execute'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(administrativeLeakage)
                .isZero();

        Integer bootstrapAdminTeacherRole =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_role ur
                        JOIN eiam_role r
                          ON r.id = ur.role_id
                         AND r.tenant_id = ur.tenant_id
                        WHERE ur.tenant_id = ?
                          AND ur.user_id = ?
                          AND r.code = 'TEACHER'
                          AND ur.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        tenant.administratorUserId()
                );

        assertThat(bootstrapAdminTeacherRole)
                .isZero();
    }
}
