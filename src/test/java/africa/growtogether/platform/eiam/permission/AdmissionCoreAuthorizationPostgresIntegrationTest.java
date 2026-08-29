package africa.growtogether.platform.eiam.permission;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class AdmissionCoreAuthorizationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_admission_core_auth_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateDatabase() {

        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .target(
                        MigrationVersion.fromVersion("163")
                )
                .load()
                .migrate();

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(
                postgres.getJdbcUrl()
        );

        dataSource.setUsername(
                postgres.getUsername()
        );

        dataSource.setPassword(
                postgres.getPassword()
        );

        jdbc =
                new JdbcTemplate(
                        dataSource
                );

        Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .load()
                .migrate();
    }

    @Test
    void definesExactlyTwoAdmissionCoreAuthorities() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission p
                        JOIN eiam_tenant t
                          ON t.id = p.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND p.code IN (
                              'school.admission.application.manage',
                              'school.admission.guardian.manage'
                          )
                          AND p.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(2);
    }

    @Test
    void schoolAdministratorReceivesBothAdmissionCoreAuthorities() {

        Integer count =
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
                        JOIN eiam_tenant t
                          ON t.id = rp.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'SCHOOL_ADMIN'
                          AND p.code IN (
                              'school.admission.application.manage',
                              'school.admission.guardian.manage'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(2);
    }

    @Test
    void integrationAdministratorReceivesNoAdmissionCoreAuthority() {

        Integer count =
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
                        JOIN eiam_tenant t
                          ON t.id = rp.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'INTEGRATION_ADMIN'
                          AND p.code IN (
                              'school.admission.application.manage',
                              'school.admission.guardian.manage'
                          )
                        """,
                        Integer.class
                );

        assertThat(count)
                .isZero();
    }

    @Test
    void migration164IsRecordedSuccessfully() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '164'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(1);
    }
}
