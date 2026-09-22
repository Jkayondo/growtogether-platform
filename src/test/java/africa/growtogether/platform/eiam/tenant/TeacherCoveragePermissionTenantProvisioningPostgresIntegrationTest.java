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
class TeacherCoveragePermissionTenantProvisioningPostgresIntegrationTest {

    private static final String READ_PERMISSION =
            "school.teacher.coverage.read";

    private static final String UPDATE_PERMISSION =
            "school.teacher.coverage.update";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_teacher_coverage_provisioning_test"
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
    void newlyProvisionedTenantReceivesDedicatedTeacherCoveragePermissions() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-COVERAGE-TEST-ORG",
                                "GT Coverage Test Organisation",
                                "GT-COVERAGE-TEST",
                                "GT Coverage Test Tenant",
                                "coverage-admin@growtogether.africa",
                                "coverage-admin",
                                "Strong-Test-Password-2026!",
                                "Coverage Tenant Administrator"
                        )
                );

        Integer permissionDefinitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code IN (?, ?)
                          AND module = 'SCHOOL_TEACHER'
                          AND system_permission = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(permissionDefinitions)
                .isEqualTo(2);

        Integer teacherRole =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'TEACHER'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(teacherRole)
                .isEqualTo(1);

        Integer teacherCoverageGrants =
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
                          AND p.code IN (?, ?)
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(teacherCoverageGrants)
                .isEqualTo(2);

        Integer nonTeacherCoverageGrants =
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
                          AND p.code IN (?, ?)
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        READ_PERMISSION,
                        UPDATE_PERMISSION
                );

        assertThat(nonTeacherCoverageGrants)
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
