package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileRepository;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

import org.junit.jupiter.api.AfterEach;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class ConnectExistingSchoolSpaceBackfillPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_school_connect_backfill_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    @DynamicPropertySource
    static void databaseProperties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );

        /*
         * Start deliberately one migration before the backfill.
         */
        registry.add(
                "spring.flyway.target",
                () -> "167"
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

        /*
         * This test intentionally boots against the historical
         * V167 schema and then applies V168 manually.
         *
         * Hibernate must therefore not validate entities introduced
         * by later migrations such as V169 during context startup.
         */
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "none"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private SchoolProfileRepository schoolProfiles;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void migration168BackfillsExistingSchoolProfileExactlyOnce() {

        UUID tenantId =
                UUID.randomUUID();

        createTenantFixture(
                tenantId
        );

        RequestContextHolder.set(
                new RequestContext(
                        "gt-connect-school-backfill-test",
                        tenantId.toString()
                )
        );

        SchoolProfile profile =
                new SchoolProfile(
                        "OLD-SCHOOL",
                        "Existing School Before GT Connect",
                        "Existing School Before GT Connect Ltd",
                        "Uganda",
                        "UG",
                        "UGX",
                        "Africa/Kampala",
                        "existing@example.com",
                        "+256700000001",
                        "https://example.com"
                );

        profile.setTenantId(
                tenantId
        );

        SchoolProfile saved =
                schoolProfiles.saveAndFlush(
                        profile
                );

        Integer before =
                canonicalSpaceCount(
                        tenantId,
                        saved.getId()
                );

        assertEquals(
                0,
                before
        );

        Flyway migration =
                Flyway.configure()
                        .dataSource(
                                POSTGRES.getJdbcUrl(),
                                POSTGRES.getUsername(),
                                POSTGRES.getPassword()
                        )
                        .target(
                                MigrationVersion.fromVersion(
                                        "168"
                                )
                        )
                        .load();

        migration.migrate();

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '168'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );

        Integer after =
                canonicalSpaceCount(
                        tenantId,
                        saved.getId()
                );

        assertEquals(
                1,
                after
        );

        String name =
                jdbc.queryForObject(
                        """
                        SELECT name
                        FROM gt_connect_spaces
                        WHERE tenant_id = ?
                          AND space_type = 'INSTITUTION'
                          AND context_type = 'SCHOOL_PROFILE'
                          AND context_reference = ?
                        """,
                        String.class,
                        tenantId,
                        saved.getId().toString()
                );

        assertEquals(
                "Existing School Before GT Connect",
                name
        );

        String createdBy =
                jdbc.queryForObject(
                        """
                        SELECT created_by
                        FROM gt_connect_spaces
                        WHERE tenant_id = ?
                          AND space_type = 'INSTITUTION'
                          AND context_type = 'SCHOOL_PROFILE'
                          AND context_reference = ?
                        """,
                        String.class,
                        tenantId,
                        saved.getId().toString()
                );

        assertEquals(
                "system",
                createdBy
        );
    }

    private void createTenantFixture(
            UUID tenantId
    ) {

        UUID organizationId =
                UUID.randomUUID();

        String suffix =
                tenantId
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                "BACKFILL-ORG-" + suffix,
                "GT Connect Backfill Test Organization"
        );

        jdbc.update(
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                "BACKFILL-TENANT-" + suffix,
                "GT Connect Backfill Test Tenant"
        );
    }

    private Integer canonicalSpaceCount(
            UUID tenantId,
            UUID schoolProfileId
    ) {

        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM gt_connect_spaces
                WHERE tenant_id = ?
                  AND space_type = 'INSTITUTION'
                  AND context_type = 'SCHOOL_PROFILE'
                  AND context_reference = ?
                """,
                Integer.class,
                tenantId,
                schoolProfileId.toString()
        );
    }
}
