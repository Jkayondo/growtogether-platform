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
class TeacherProgrammePermissionTenantProvisioningPostgresIntegrationTest {

    private static final String PROGRAMME_PERMISSION =
            "school.teacher.programme.read";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_teacher_programme_provisioning_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

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
    void newlyProvisionedTenantReceivesDedicatedTeacherProgrammeReadPermission() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-PROGRAMME-PERM-TEST-ORG",
                                "GT Programme Permission Test Organisation",
                                "GT-PROGRAMME-PERM-TEST",
                                "GT Programme Permission Test Tenant",
                                "programme-permission-admin@growtogether.africa",
                                "programme-permission-admin",
                                "Strong-Test-Password-2026!",
                                "Programme Permission Tenant Administrator"
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

        Integer canonicalDefinition =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code = ?
                          AND name = 'Read Teacher Programme'
                          AND module = 'SCHOOL_TEACHER'
                          AND description =
                              'Allows an authenticated teacher to view their own authorised Today''s Programme, including applicable teaching lessons and teacher-visible school calendar events within their authenticated tenant.'
                          AND system_permission = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        PROGRAMME_PERMISSION
                );

        assertThat(canonicalDefinition)
                .isEqualTo(1);

        Integer teacherGrant =
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
                          AND r.status = 'ACTIVE'
                          AND p.code = ?
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        PROGRAMME_PERMISSION
                );

        assertThat(teacherGrant)
                .isEqualTo(1);

        Integer nonTeacherGrant =
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
                          AND r.code <> 'TEACHER'
                          AND p.code = ?
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        PROGRAMME_PERMISSION
                );

        assertThat(nonTeacherGrant)
                .isZero();

        Integer teacherCalendarReadGrant =
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
                          AND r.status = 'ACTIVE'
                          AND p.code = 'school.academic.calendar.read'
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(teacherCalendarReadGrant)
                .isZero();

        Integer administratorTeacherRole =
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

        assertThat(administratorTeacherRole)
                .isZero();
    }
}
