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
class TenantProvisioningAiAdminAuthorizationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_ai_admin_provisioning_test"
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
    void newlyProvisionedTenantReceivesDedicatedAiAdminFoundation() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-AI-ADMIN-PROVISION-ORG",
                                "GT AI Admin Provision Organisation",
                                "GT-AI-ADMIN-PROVISION",
                                "GT AI Admin Provision Tenant",
                                "ai-admin-provision@growtogether.africa",
                                "ai-admin-provision",
                                "Strong-Test-Password-2026!",
                                "AI Admin Provision Administrator"
                        )
                );

        Integer roles =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'AI_ADMIN'
                          AND system_role = TRUE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(roles)
                .isEqualTo(1);

        Integer canonicalDefinitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND module = 'EAIF'
                          AND system_permission = TRUE
                          AND status = 'ACTIVE'
                          AND (
                            (
                              code = 'ai.provider.manage'
                              AND name = 'AI Provider Manage'
                              AND description = 'Manage governed enterprise AI provider registrations.'
                            )
                            OR (
                              code = 'ai.model.manage'
                              AND name = 'AI Model Manage'
                              AND description = 'Manage governed enterprise AI model catalogue entries.'
                            )
                            OR (
                              code = 'ai.prompt.manage'
                              AND name = 'AI Prompt Manage'
                              AND description = 'Manage governed enterprise AI prompt templates and controls.'
                            )
                            OR (
                              code = 'ai.governance.read'
                              AND name = 'AI Governance Read'
                              AND description = 'Read governed enterprise AI governance policy and control state.'
                            )
                            OR (
                              code = 'ai.audit.read'
                              AND name = 'AI Audit Read'
                              AND description = 'Read governed enterprise AI audit records.'
                            )
                            OR (
                              code = 'ai.evidence.read'
                              AND name = 'AI Evidence Read'
                              AND description = 'Read governed enterprise AI execution evidence.'
                            )
                            OR (
                              code = 'ai.request.approval'
                              AND name = 'AI Request Approval'
                              AND description = 'Approve governed enterprise AI requests requiring human authorization.'
                            )
                          )
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(canonicalDefinitions)
                .isEqualTo(7);

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
                          AND r.code = 'AI_ADMIN'
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
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(grants)
                .isEqualTo(8);

        Integer forbiddenRuntime =
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
                          AND r.code = 'AI_ADMIN'
                          AND rp.status = 'ACTIVE'
                          AND p.code IN (
                            'ai.request.create',
                            'ai.runtime.execute'
                          )
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(forbiddenRuntime)
                .isZero();

        Integer forbiddenIntegration =
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
                          AND r.code = 'AI_ADMIN'
                          AND rp.status = 'ACTIVE'
                          AND p.code LIKE 'integration.%'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(forbiddenIntegration)
                .isZero();

        Integer automaticAssignment =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_role ur
                        JOIN eiam_role r
                          ON r.id = ur.role_id
                         AND r.tenant_id = ur.tenant_id
                        WHERE ur.tenant_id = ?
                          AND ur.user_id = ?
                          AND r.code = 'AI_ADMIN'
                          AND ur.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        tenant.administratorUserId()
                );

        assertThat(automaticAssignment)
                .isZero();

        Integer privilegeLeakage =
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
                            'TEACHER'
                          )
                          AND rp.status = 'ACTIVE'
                          AND p.code IN (
                            'ai.provider.manage',
                            'ai.model.manage',
                            'ai.prompt.manage',
                            'ai.governance.read',
                            'ai.audit.read',
                            'ai.evidence.read',
                            'ai.request.approval'
                          )
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(privilegeLeakage)
                .isZero();
    }
}
