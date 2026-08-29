package africa.growtogether.platform.eiam.tenant;

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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class TenantProvisioningIntegrationAdminPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_tenant_eip_provisioning_test"
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
    void newlyProvisionedTenantReceivesGovernedIntegrationFoundation() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-EIP-TEST-ORG",
                                "GT EIP Test Organisation",
                                "GT-EIP-TEST",
                                "GT EIP Test Tenant",
                                "future-admin@growtogether.africa",
                                "future-admin",
                                "Strong-Test-Password-2026!",
                                "Future Tenant Administrator"
                        )
                );

        Integer permissionCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code LIKE 'integration.%'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(permissionCount)
                .isEqualTo(25);

        Integer integrationRoleCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'INTEGRATION_ADMIN'
                          AND system_role = TRUE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(integrationRoleCount)
                .isEqualTo(1);

        Integer infrastructureAssignmentCount =
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
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code LIKE 'integration.%'
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(infrastructureAssignmentCount)
                .isEqualTo(17);

        Integer financialAssignmentCount =
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
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code IN (
                              'integration.payment.create',
                              'integration.payment.execute',
                              'integration.payment.manage',
                              'integration.payment.read',
                              'integration.payment.reverse',
                              'integration.settlement.manage',
                              'integration.reconciliation.manage',
                              'integration.dispute.manage'
                          )
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(financialAssignmentCount)
                .isZero();

        Integer initialAdminIntegrationRoleCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_role ur
                        JOIN eiam_role r
                          ON r.id = ur.role_id
                         AND r.tenant_id = ur.tenant_id
                        WHERE ur.tenant_id = ?
                          AND ur.user_id = ?
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND ur.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId(),
                        tenant.administratorUserId()
                );

        assertThat(initialAdminIntegrationRoleCount)
                .isZero();
    }

    @Test
    void newlyProvisionedTenantReceivesGovernedNotificationRoutingAuthorization() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-ENS-TEST-ORG",
                                "GT ENS Test Organisation",
                                "GT-ENS-TEST",
                                "GT ENS Test Tenant",
                                "ens-admin@growtogether.africa",
                                "ens-admin",
                                "Strong-Test-Password-2026!",
                                "ENS Future Tenant Administrator"
                        )
                );

        Integer permissionCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission
                        WHERE tenant_id = ?
                          AND code LIKE 'notification.%'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(permissionCount)
                .isEqualTo(6);

        Integer governedNotificationAssignmentCount =
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
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code IN (
                              'notification.dispatch.manage',
                              'notification.route.read',
                              'notification.route.manage'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(governedNotificationAssignmentCount)
                .isEqualTo(3);

        Integer operationalAssignmentCount =
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
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code IN (
                              'notification.send',
                              'notification.read',
                              'notification.queue.manage'
                          )
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(operationalAssignmentCount)
                .isZero();
    }

}
