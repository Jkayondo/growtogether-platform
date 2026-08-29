package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
class ConnectSchoolProfileSpacePostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_school_connect_space_test"
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
    private ConnectSpaceRepository spaces;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void flywayAppliesCanonicalSchoolProfileSpaceMigration() {

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '167'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );
    }

    @Test
    void oneSchoolProfileCanHaveOnlyOneCanonicalInstitutionSpace() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        activateTenant(
                tenantId
        );

        spaces.saveAndFlush(
                schoolSpace(
                        tenantId,
                        schoolProfileId,
                        "School Institution Space"
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> spaces.saveAndFlush(
                        schoolSpace(
                                tenantId,
                                schoolProfileId,
                                "Duplicate School Institution Space"
                        )
                )
        );
    }

    @Test
    void differentSchoolProfileReferencesCanHaveSeparateInstitutionSpaces() {

        UUID tenantId =
                UUID.randomUUID();

        activateTenant(
                tenantId
        );

        ConnectSpace first =
                spaces.saveAndFlush(
                        schoolSpace(
                                tenantId,
                                UUID.randomUUID(),
                                "School One"
                        )
                );

        ConnectSpace second =
                spaces.saveAndFlush(
                        schoolSpace(
                                tenantId,
                                UUID.randomUUID(),
                                "School Two"
                        )
                );

        assertNotNull(
                first.getId()
        );

        assertNotNull(
                second.getId()
        );

        assertNotEquals(
                first.getId(),
                second.getId()
        );
    }

    @Test
    void sameSchoolProfileReferenceCanExistInDifferentTenants() {

        UUID sharedProfileReference =
                UUID.randomUUID();

        UUID firstTenant =
                UUID.randomUUID();

        UUID secondTenant =
                UUID.randomUUID();

        activateTenant(
                firstTenant
        );

        ConnectSpace first =
                spaces.saveAndFlush(
                        schoolSpace(
                                firstTenant,
                                sharedProfileReference,
                                "Tenant One School"
                        )
                );

        activateTenant(
                secondTenant
        );

        ConnectSpace second =
                spaces.saveAndFlush(
                        schoolSpace(
                                secondTenant,
                                sharedProfileReference,
                                "Tenant Two School"
                        )
                );

        assertNotNull(
                first.getId()
        );

        assertNotNull(
                second.getId()
        );
    }

    @Test
    void nonInstitutionSpaceDoesNotConsumeCanonicalInstitutionMapping() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        activateTenant(
                tenantId
        );

        ConnectSpace group =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                tenantId,
                                ConnectSpaceType.GROUP,
                                "School Working Group",
                                "SCHOOL_PROFILE",
                                schoolProfileId.toString()
                        )
                );

        ConnectSpace institution =
                spaces.saveAndFlush(
                        schoolSpace(
                                tenantId,
                                schoolProfileId,
                                "Canonical Institution"
                        )
                );

        assertNotNull(
                group.getId()
        );

        assertNotNull(
                institution.getId()
        );
    }

    private ConnectSpace schoolSpace(
            UUID tenantId,
            UUID schoolProfileId,
            String name
    ) {

        return new ConnectSpace(
                tenantId,
                ConnectSpaceType.INSTITUTION,
                name,
                "SCHOOL_PROFILE",
                schoolProfileId.toString()
        );
    }

    private void activateTenant(
            UUID tenantId
    ) {

        RequestContextHolder.set(
                new RequestContext(
                        "school-connect-space-test",
                        tenantId.toString()
                )
        );
    }
}
