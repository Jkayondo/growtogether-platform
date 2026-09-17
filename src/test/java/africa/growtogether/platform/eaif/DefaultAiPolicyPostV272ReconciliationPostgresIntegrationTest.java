package africa.growtogether.platform.eaif;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.Objects;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class DefaultAiPolicyPostV272ReconciliationPostgresIntegrationTest {

    private static final String MIGRATION =
            "db/migration/"
                    + "V273__reconcile_post_v272_default_ai_policy.sql";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_v273_policy_reconciliation_test"
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
    private JdbcTemplate jdbc;

    @Test
    void reconciliationRepairsOnlyMissingPolicyAndIsIdempotent()
            throws Exception {

        UUID tenantId =
                jdbc.queryForObject(
                        """
                        SELECT tenant_id
                        FROM ai_governance_policies
                        WHERE policy_code = 'DEFAULT_AI_POLICY'
                        ORDER BY tenant_id
                        LIMIT 1
                        """,
                        UUID.class
                );

        assertThat(tenantId)
                .isNotNull();

        Integer providersBefore =
                count("SELECT COUNT(*) FROM eaif_providers");

        Integer modelsBefore =
                count("SELECT COUNT(*) FROM eaif_models");

        Integer connectorsBefore =
                count("SELECT COUNT(*) FROM eip_external_connectors");

        Integer userRolesBefore =
                count("SELECT COUNT(*) FROM eiam_user_role");

        Integer explicitValuesBefore =
                count(
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
                        """
                );

        int deleted =
                jdbc.update(
                        """
                        DELETE FROM ai_governance_policies
                        WHERE tenant_id = ?
                          AND policy_code = 'DEFAULT_AI_POLICY'
                        """,
                        tenantId
                );

        assertThat(deleted)
                .isEqualTo(1);

        Integer missingBeforeRepair =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_tenant t
                        WHERE t.id = ?
                          AND NOT EXISTS (
                            SELECT 1
                            FROM ai_governance_policies p
                            WHERE p.tenant_id = t.id
                              AND p.policy_code = 'DEFAULT_AI_POLICY'
                          )
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(missingBeforeRepair)
                .isEqualTo(1);

        executeMigration();

        assertSafeReconciledPolicy(tenantId);

        Integer repairedByV273 =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ai_governance_policies
                        WHERE tenant_id = ?
                          AND policy_code = 'DEFAULT_AI_POLICY'
                          AND created_by = 'GT-MIGRATION-V273'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(repairedByV273)
                .isEqualTo(1);

        executeMigration();

        Integer afterSecondRun =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ai_governance_policies
                        WHERE tenant_id = ?
                          AND policy_code = 'DEFAULT_AI_POLICY'
                        """,
                        Integer.class,
                        tenantId
                );

        assertThat(afterSecondRun)
                .isEqualTo(1);

        assertThat(count("SELECT COUNT(*) FROM eaif_providers"))
                .isEqualTo(providersBefore);

        assertThat(count("SELECT COUNT(*) FROM eaif_models"))
                .isEqualTo(modelsBefore);

        assertThat(count("SELECT COUNT(*) FROM eip_external_connectors"))
                .isEqualTo(connectorsBefore);

        assertThat(count("SELECT COUNT(*) FROM eiam_user_role"))
                .isEqualTo(userRolesBefore);

        assertThat(
                count(
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
                        """
                )
        ).isEqualTo(explicitValuesBefore);
    }

    private void assertSafeReconciledPolicy(
            UUID tenantId
    ) {
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
                        tenantId
                );

        assertThat(rows)
                .isEqualTo(1);
    }

    private void executeMigration()
            throws Exception {

        DataSource dataSource =
                Objects.requireNonNull(
                        jdbc.getDataSource()
                );

        try (Connection connection =
                     dataSource.getConnection()) {

            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(MIGRATION)
            );
        }
    }

    private Integer count(
            String sql
    ) {
        return jdbc.queryForObject(
                sql,
                Integer.class
        );
    }
}
