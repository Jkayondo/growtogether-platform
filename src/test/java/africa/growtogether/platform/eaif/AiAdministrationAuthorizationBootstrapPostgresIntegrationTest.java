package africa.growtogether.platform.eaif;

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
class AiAdministrationAuthorizationBootstrapPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_ai_admin_authorization_test"
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
    private JdbcTemplate jdbc;

    @Test
    void everyTenantReceivesDedicatedGovernedAiAdministrationFoundation() {

        Integer tenants =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_tenant
                        """,
                        Integer.class
                );

        assertThat(tenants)
                .isNotNull()
                .isPositive();

        Integer aiAdminRoles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE code = 'AI_ADMIN'
                          AND system_role = TRUE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(aiAdminRoles)
                .isEqualTo(tenants);

        Integer adminPermissionDefinitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE module = 'EAIF'
                          AND system_permission = TRUE
                          AND status = 'ACTIVE'
                          AND code IN (
                            'ai.provider.manage',
                            'ai.model.manage',
                            'ai.prompt.manage',
                            'ai.governance.read',
                            'ai.audit.read',
                            'ai.evidence.read',
                            'ai.request.approval'
                          )
                        """,
                        Integer.class
                );

        assertThat(adminPermissionDefinitions)
                .isEqualTo(tenants * 7);

        Integer aiAdminGrants =
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
                        WHERE r.code = 'AI_ADMIN'
                          AND r.status = 'ACTIVE'
                          AND rp.status = 'ACTIVE'
                          AND p.status = 'ACTIVE'
                          AND p.code IN (
                            'ai.provider.manage',
                            'ai.model.manage',
                            'ai.prompt.manage',
                            'ai.governance.read',
                            'ai.audit.read',
                            'ai.evidence.read',
                            'ai.request.approval',
                            'ai.request.read'
                          )
                        """,
                        Integer.class
                );

        assertThat(aiAdminGrants)
                .isEqualTo(tenants * 8);

        Integer forbiddenRuntimeGrants =
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
                        WHERE r.code = 'AI_ADMIN'
                          AND rp.status = 'ACTIVE'
                          AND p.code IN (
                            'ai.request.create',
                            'ai.runtime.execute'
                          )
                        """,
                        Integer.class
                );

        assertThat(forbiddenRuntimeGrants)
                .isZero();

        Integer forbiddenIntegrationGrants =
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
                        WHERE r.code = 'AI_ADMIN'
                          AND rp.status = 'ACTIVE'
                          AND p.code LIKE 'integration.%'
                        """,
                        Integer.class
                );

        assertThat(forbiddenIntegrationGrants)
                .isZero();

        Integer automaticUserAssignments =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_role ur
                        JOIN eiam_role r
                          ON r.id = ur.role_id
                         AND r.tenant_id = ur.tenant_id
                        WHERE r.code = 'AI_ADMIN'
                          AND ur.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(automaticUserAssignments)
                .isZero();
    }
}
