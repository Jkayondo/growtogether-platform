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
class EaifSafeConfigurationGovernanceBootstrapPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_eaif_safe_governance_test"
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
    void safeEaifConfigurationDefinitionsMatchRuntimeFallbackContract() {

        Integer definitions =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ecs_configuration_definitions
                        WHERE active = TRUE
                          AND category = 'EAIF'
                          AND (
                            (
                              code = 'EAIF_PROVIDER_EXECUTION_ENABLED'
                              AND data_type = 'BOOLEAN'
                              AND default_value = 'false'
                              AND required = TRUE
                              AND secret_value = FALSE
                            )
                            OR (
                              code = 'EAIF_HIGH_RISK_APPROVAL_REQUIRED'
                              AND data_type = 'BOOLEAN'
                              AND default_value = 'true'
                              AND required = TRUE
                              AND secret_value = FALSE
                            )
                            OR (
                              code = 'EAIF_MAX_INPUT_CHARACTERS'
                              AND data_type = 'INTEGER'
                              AND default_value = '100000'
                              AND required = TRUE
                              AND secret_value = FALSE
                            )
                            OR (
                              code = 'EAIF_REQUEST_RETENTION_DAYS'
                              AND data_type = 'INTEGER'
                              AND default_value = '90'
                              AND required = TRUE
                              AND secret_value = FALSE
                            )
                            OR (
                              code = 'EAIF_DEFAULT_MODEL_CODE'
                              AND data_type = 'STRING'
                              AND default_value = ''
                              AND required = FALSE
                              AND secret_value = FALSE
                            )
                          )
                        """,
                        Integer.class
                );

        assertThat(definitions)
                .isEqualTo(5);

        Integer externalDeliverySafe =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ecs_configuration_definitions
                        WHERE code = 'EIP_EXTERNAL_DELIVERY_ENABLED'
                          AND active = TRUE
                          AND data_type = 'BOOLEAN'
                          AND default_value = 'false'
                        """,
                        Integer.class
                );

        assertThat(externalDeliverySafe)
                .isEqualTo(1);
    }

    @Test
    void everyExistingTenantReceivesHumanGovernedDefaultAiPolicy() {

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

        Integer policies =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ai_governance_policies
                        WHERE policy_code = 'DEFAULT_AI_POLICY'
                          AND policy_name = 'Default AI Governance Policy'
                          AND maximum_risk_level = 'HIGH'
                          AND approval_required = TRUE
                          AND active = TRUE
                          AND status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(policies)
                .isEqualTo(tenants);
    }

    @Test
    void v272DoesNotCreateRuntimeProviderModelOrConnectorConfiguration() {

        Integer providerRows =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eaif_providers
                        """,
                        Integer.class
                );

        Integer modelRows =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eaif_models
                        """,
                        Integer.class
                );

        assertThat(providerRows)
                .isZero();

        assertThat(modelRows)
                .isZero();

        Integer eaifValues =
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

        assertThat(eaifValues)
                .isZero();
    }
}
