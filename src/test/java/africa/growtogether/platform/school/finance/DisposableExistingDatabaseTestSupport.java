package africa.growtogether.platform.school.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.containers.PostgreSQLContainer;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class DisposableExistingDatabaseTestSupport {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    private DataSource disposableDataSource;

    @Autowired
    private JdbcTemplate disposableJdbc;

    @DynamicPropertySource
    static void bindDisposablePostgres(
            DynamicPropertyRegistry registry
    ) {

        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
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
    }

    @BeforeEach
    void verifyBindingAndSeedRootFixture()
            throws Exception {

        verifyDisposableBinding();

        UUID primaryTenant =
                disposableJdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        ORDER BY id
                        LIMIT 1
                        """,
                        UUID.class
                );

        assertNotNull(
                primaryTenant,
                "Migrated disposable database must contain the seeded GT tenant."
        );

        UUID actor =
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"
                );

        seedSchoolRoot(
                primaryTenant,
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111112"
                ),
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111113"
                ),
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111114"
                ),
                actor,
                "I16A"
        );

        UUID otherOrganization =
                UUID.fromString(
                        "ffffffff-ffff-ffff-ffff-ffffffffffe0"
                );

        UUID otherTenant =
                UUID.fromString(
                        "ffffffff-ffff-ffff-ffff-fffffffffff0"
                );

        disposableJdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT DO NOTHING
                """,
                otherOrganization,
                "I16ORG2",
                "Item 16 Disposable Organisation 2"
        );

        disposableJdbc.update(
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
                VALUES (?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, 0)
                ON CONFLICT DO NOTHING
                """,
                otherTenant,
                otherOrganization,
                "I16TEN2",
                "Item 16 Disposable Tenant 2"
        );

        seedSchoolRoot(
                otherTenant,
                UUID.fromString(
                        "ffffffff-ffff-ffff-ffff-ffffffffffa1"
                ),
                UUID.fromString(
                        "ffffffff-ffff-ffff-ffff-ffffffffffa2"
                ),
                UUID.fromString(
                        "ffffffff-ffff-ffff-ffff-ffffffffffa3"
                ),
                actor,
                "I16B"
        );

        System.out.println(
                "ITEM16_DISPOSABLE_DB_BINDING=VERIFIED"
                + "|TEST_CLASS="
                + getClass().getSimpleName()
                + "|POSTGRES_IMAGE=postgres:17-alpine"
                + "|DATABASE=test"
        );
    }

    private void verifyDisposableBinding()
            throws Exception {

        try (
                Connection connection =
                        disposableDataSource.getConnection()
        ) {
            String actual =
                    stripQuery(
                            connection
                                    .getMetaData()
                                    .getURL()
                    );

            String expected =
                    stripQuery(
                            POSTGRES.getJdbcUrl()
                    );

            assertEquals(
                    expected,
                    actual,
                    "Spring datasource is not bound to the disposable Testcontainers PostgreSQL database."
            );
        }
    }

    private void seedSchoolRoot(
            UUID tenantId,
            UUID schoolId,
            UUID academicYearId,
            UUID studentId,
            UUID actor,
            String code
    ) {
        disposableJdbc.update(
                """
                INSERT INTO gts_school_profile (
                    id,
                    tenant_id,
                    school_code,
                    school_name,
                    country_code,
                    default_currency,
                    timezone,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'UG', 'UGX', 'Africa/Kampala',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                ON CONFLICT DO NOTHING
                """,
                schoolId,
                tenantId,
                "SCH-" + code,
                "Item 16 Disposable School " + code,
                actor,
                actor
        );

        disposableJdbc.update(
                """
                INSERT INTO gts_academic_year (
                    id,
                    tenant_id,
                    academic_year_code,
                    academic_year_name,
                    start_date,
                    end_date,
                    current_year,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                ON CONFLICT DO NOTHING
                """,
                academicYearId,
                tenantId,
                "AY-" + code,
                "2026 " + code,
                actor,
                actor
        );

        disposableJdbc.update(
                """
                INSERT INTO gts_student (
                    id,
                    tenant_id,
                    student_number,
                    permanent_learner_number,
                    first_name,
                    last_name,
                    date_of_birth,
                    student_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'Item16',
                    ?,
                    DATE '2015-01-01',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                ON CONFLICT DO NOTHING
                """,
                studentId,
                tenantId,
                "STD-" + code,
                "PLN-" + code,
                "Student-" + code,
                actor,
                actor
        );
    }

    private static String stripQuery(
            String value
    ) {
        int question =
                value.indexOf('?');

        if (question < 0) {
            return value;
        }

        return value.substring(
                0,
                question
        );
    }

    @AfterAll
    static void stopDisposablePostgres() {
        if (POSTGRES.isRunning()) {
            POSTGRES.stop();
        }
    }
}
