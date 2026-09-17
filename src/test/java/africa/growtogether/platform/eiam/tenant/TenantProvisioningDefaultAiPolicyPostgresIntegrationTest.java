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
class TenantProvisioningDefaultAiPolicyPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName(
                            "growtogether_default_ai_policy_test"
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
    void newlyProvisionedTenantReceivesSafeDefaultAiPolicy() {

        TenantView tenant =
                service.provision(
                        new ProvisionTenantCommand(
                                "GT-AI-POLICY-ORG",
                                "GT AI Policy Organisation",
                                "GT-AI-POLICY",
                                "GT AI Policy Tenant",
                                "ai-policy-admin@growtogether.africa",
                                "ai-policy-admin",
                                "Strong-Test-Password-2026!",
                                "AI Policy Tenant Administrator"
                        )
                );

        Integer rows =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ai_governance_policies
                        WHERE tenant_id = ?
                          AND policy_code = 'DEFAULT_AI_POLICY'
                          AND policy_name = 'Default AI Governance Policy'
                          AND maximum_risk_level = 'HIGH'
                          AND approval_required = TRUE
                          AND active = TRUE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(rows)
                .isEqualTo(1);

        Integer allPolicies =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ai_governance_policies
                        WHERE tenant_id = ?
                          AND policy_code = 'DEFAULT_AI_POLICY'
                        """,
                        Integer.class,
                        tenant.tenantId()
                );

        assertThat(allPolicies)
                .isEqualTo(1);

        Integer providers =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM eaif_providers",
                        Integer.class
                );

        Integer models =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM eaif_models",
                        Integer.class
                );

        assertThat(providers).isZero();
        assertThat(models).isZero();

        Integer explicitValues =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ecs_configuration_values v
                        JOIN ecs_configuration_definitions d
                          ON d.id = v.definition_id
                        WHERE d.code IN (
                          'EAIF_PROVIDER_EXECUTION_ENABLED',
                          'EAIF_HIGH_RISK_APPROVAL_REQUIRED',
                          'EAIF_MAX_INPUT_CHARACTERS',
                          'EAIF_REQUEST_RETENTION_DAYS',
                          'EAIF_DEFAULT_MODEL_CODE'
                        )
                        """,
                        Integer.class
                );

        assertThat(explicitValues)
                .isZero();
    }
}
