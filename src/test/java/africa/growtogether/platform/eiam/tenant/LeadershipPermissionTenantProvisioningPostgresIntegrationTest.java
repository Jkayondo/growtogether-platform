package africa.growtogether.platform.eiam.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

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
class LeadershipPermissionTenantProvisioningPostgresIntegrationTest {

    private static final String LEADERSHIP_PERMISSION =
            "school.leadership.overview.read";

    private static final List<String> PROVISIONED_ROLE_CODES =
            List.of(
                    "TENANT_ADMIN",
                    "INTEGRATION_ADMIN",
                    "TEACHER",
                    "AI_ADMIN"
            );

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_leadership_permission_test"
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
    void newlyProvisionedTenantsReceiveLeadershipPermissionWithoutRoleGrant() {

        TenantView first =
                provision(
                        "GT-LEADERSHIP-PERM-A-ORG",
                        "GT Leadership Permission A Organisation",
                        "GT-LEADERSHIP-PERM-A",
                        "GT Leadership Permission A Tenant",
                        "leadership-permission-a-admin@growtogether.africa",
                        "leadership-permission-a-admin",
                        "Leadership Permission A Administrator"
                );

        TenantView second =
                provision(
                        "GT-LEADERSHIP-PERM-B-ORG",
                        "GT Leadership Permission B Organisation",
                        "GT-LEADERSHIP-PERM-B",
                        "GT Leadership Permission B Tenant",
                        "leadership-permission-b-admin@growtogether.africa",
                        "leadership-permission-b-admin",
                        "Leadership Permission B Administrator"
                );

        assertCanonicalPermission(first.tenantId());
        assertCanonicalPermission(second.tenantId());

        UUID firstPermissionId =
                permissionId(first.tenantId());

        UUID secondPermissionId =
                permissionId(second.tenantId());

        assertThat(firstPermissionId)
                .isNotEqualTo(secondPermissionId);

        assertNoRoleGrant(first.tenantId());
        assertNoRoleGrant(second.tenantId());

        for (String roleCode : PROVISIONED_ROLE_CODES) {

            assertRoleExists(
                    first.tenantId(),
                    roleCode
            );

            assertRoleExists(
                    second.tenantId(),
                    roleCode
            );

            assertRoleHasNoLeadershipPermission(
                    first.tenantId(),
                    roleCode
            );

            assertRoleHasNoLeadershipPermission(
                    second.tenantId(),
                    roleCode
            );
        }

        Integer firstPermissionVisibleInSecondTenant =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        Integer.class,
                        second.tenantId(),
                        firstPermissionId
                );

        assertThat(firstPermissionVisibleInSecondTenant)
                .isZero();

        Integer secondPermissionVisibleInFirstTenant =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        Integer.class,
                        first.tenantId(),
                        secondPermissionId
                );

        assertThat(secondPermissionVisibleInFirstTenant)
                .isZero();

        Integer canonicalDefinitionsAcrossTestTenants =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id IN (?, ?)
                          AND code = ?
                          AND module = 'SCHOOL_LEADERSHIP'
                          AND system_permission = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        first.tenantId(),
                        second.tenantId(),
                        LEADERSHIP_PERMISSION
                );

        assertThat(canonicalDefinitionsAcrossTestTenants)
                .isEqualTo(2);
    }

    private TenantView provision(
            String organizationCode,
            String organizationName,
            String tenantCode,
            String tenantName,
            String administratorEmail,
            String administratorUsername,
            String administratorDisplayName
    ) {

        return service.provision(
                new ProvisionTenantCommand(
                        organizationCode,
                        organizationName,
                        tenantCode,
                        tenantName,
                        administratorEmail,
                        administratorUsername,
                        "Strong-Test-Password-2026!",
                        administratorDisplayName
                )
        );
    }

    private void assertCanonicalPermission(
            UUID tenantId
    ) {

        Integer permissionDefinitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code = ?
                          AND name = 'Read School Leadership Overview'
                          AND module = 'SCHOOL_LEADERSHIP'
                          AND description =
                              'Allows an authorised school leadership user to read the tenant-scoped GT School Leadership overview and its permitted aggregate indicators.'
                          AND system_permission = FALSE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        LEADERSHIP_PERMISSION
                );

        assertThat(permissionDefinitions)
                .isEqualTo(1);
    }

    private UUID permissionId(
            UUID tenantId
    ) {

        return jdbc.queryForObject(
                """
                SELECT id
                FROM eiam_permission
                WHERE tenant_id = ?
                  AND code = ?
                  AND status = 'ACTIVE'
                """,
                UUID.class,
                tenantId,
                LEADERSHIP_PERMISSION
        );
    }

    private void assertNoRoleGrant(
            UUID tenantId
    ) {

        Integer grants =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = rp.tenant_id
                        WHERE rp.tenant_id = ?
                          AND p.code = ?
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        LEADERSHIP_PERMISSION
                );

        assertThat(grants)
                .isZero();
    }

    private void assertRoleExists(
            UUID tenantId,
            String roleCode
    ) {

        Integer roles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = ?
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        roleCode
                );

        assertThat(roles)
                .as("Expected provisioned role %s", roleCode)
                .isEqualTo(1);
    }

    private void assertRoleHasNoLeadershipPermission(
            UUID tenantId,
            String roleCode
    ) {

        Integer grants =
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
                          AND r.code = ?
                          AND r.status = 'ACTIVE'
                          AND p.code = ?
                          AND p.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        roleCode,
                        LEADERSHIP_PERMISSION
                );

        assertThat(grants)
                .as(
                        "Role %s must not receive Leadership permission",
                        roleCode
                )
                .isZero();
    }
}
