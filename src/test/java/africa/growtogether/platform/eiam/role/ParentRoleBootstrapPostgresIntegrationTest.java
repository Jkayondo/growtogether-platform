package africa.growtogether.platform.eiam.role;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class ParentRoleBootstrapPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_parent_role_test"
                    )
                    .withUsername(
                            "gt_test"
                    )
                    .withPassword(
                            "gt_test"
                    );

    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateDatabase() {

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .load()
                .migrate();

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(
                POSTGRES.getJdbcUrl()
        );

        dataSource.setUsername(
                POSTGRES.getUsername()
        );

        dataSource.setPassword(
                POSTGRES.getPassword()
        );

        jdbc =
                new JdbcTemplate(
                        dataSource
                );
    }

    @Test
    void bootstrapsParentRoleForGtSchoolTenant() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role r
                        JOIN eiam_tenant t
                          ON t.id = r.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'PARENT'
                          AND r.name = 'Parent / Guardian'
                          AND r.system_role = TRUE
                          AND r.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                count
        );
    }

    @Test
    void parentRoleStartsWithoutPermissions() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = rp.tenant_id
                        JOIN eiam_tenant t
                          ON t.id = r.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'PARENT'
                        """,
                        Integer.class
                );

        assertEquals(
                0,
                count,
                "PARENT must not receive speculative permissions"
        );
    }
}
