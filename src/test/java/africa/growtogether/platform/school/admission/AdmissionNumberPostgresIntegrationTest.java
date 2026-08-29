package africa.growtogether.platform.school.admission;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class AdmissionNumberPostgresIntegrationTest {

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO gts_admission_number_sequence (
                tenant_id,
                academic_year_id,
                last_issued_number
            )
            VALUES (?, ?, 1)
            ON CONFLICT (tenant_id, academic_year_id)
            DO UPDATE
            SET last_issued_number =
                    gts_admission_number_sequence.last_issued_number + 1,
                updated_at = CURRENT_TIMESTAMP
            RETURNING last_issued_number
            """;

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_admission_test"
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
    void incrementsWithinAcademicYearAndRestartsForNextAcademicYear() {

        Fixture fixture =
                createFixture();

        assertEquals(
                1L,
                nextSequence(
                        fixture.tenantId(),
                        fixture.academicYear2026Id()
                )
        );

        assertEquals(
                2L,
                nextSequence(
                        fixture.tenantId(),
                        fixture.academicYear2026Id()
                )
        );

        assertEquals(
                3L,
                nextSequence(
                        fixture.tenantId(),
                        fixture.academicYear2026Id()
                )
        );

        assertEquals(
                1L,
                nextSequence(
                        fixture.tenantId(),
                        fixture.academicYear2027Id()
                )
        );

        assertEquals(
                2L,
                nextSequence(
                        fixture.tenantId(),
                        fixture.academicYear2027Id()
                )
        );
    }

    @Test
    void concurrentRequestsReceiveDistinctSequenceNumbers()
            throws Exception {

        Fixture fixture =
                createFixture();

        int requestCount = 20;
        int workerCount = 8;

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        workerCount
                );

        CountDownLatch ready =
                new CountDownLatch(
                        workerCount
                );

        CountDownLatch start =
                new CountDownLatch(
                        1
                );

        try {

            @SuppressWarnings("unchecked")
            Future<Long>[] futures =
                    new Future[requestCount];

            for (
                    int index = 0;
                    index < requestCount;
                    index++
            ) {

                futures[index] =
                        executor.submit(
                                () -> {

                                    ready.countDown();

                                    start.await();

                                    return nextSequence(
                                            fixture.tenantId(),
                                            fixture.academicYear2026Id()
                                    );
                                }
                        );
            }

            ready.await();

            start.countDown();

            Set<Long> issuedNumbers =
                    new HashSet<>();

            for (Future<Long> future : futures) {

                issuedNumbers.add(
                        future.get()
                );
            }

            assertEquals(
                    requestCount,
                    issuedNumbers.size(),
                    "Concurrent requests must receive unique numbers"
            );

            for (
                    long expected = 1;
                    expected <= requestCount;
                    expected++
            ) {

                assertTrue(
                        issuedNumbers.contains(
                                expected
                        ),
                        "Missing sequence number: "
                                + expected
                );
            }

            Long databaseValue =
                    jdbc.queryForObject(
                            """
                            SELECT last_issued_number
                            FROM gts_admission_number_sequence
                            WHERE tenant_id = ?
                              AND academic_year_id = ?
                            """,
                            Long.class,
                            fixture.tenantId(),
                            fixture.academicYear2026Id()
                    );

            assertEquals(
                    (long) requestCount,
                    databaseValue
            );

        } finally {

            executor.shutdownNow();
        }
    }

    private long nextSequence(
            UUID tenantId,
            UUID academicYearId
    ) {

        Long sequence =
                jdbc.queryForObject(
                        NEXT_SEQUENCE_SQL,
                        Long.class,
                        tenantId,
                        academicYearId
                );

        if (sequence == null) {
            throw new IllegalStateException(
                    "Admission sequence was not returned"
            );
        }

        return sequence;
    }

    private Fixture createFixture() {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID academicYear2026Id =
                UUID.randomUUID();

        UUID academicYear2027Id =
                UUID.randomUUID();

        String auditUser =
                "a12-admission-integration-test";

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                organizationId,
                "ORG-" + shortId(organizationId),
                "A12 Admission Integration Organisation"
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
                VALUES (?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, 0)
                """,
                tenantId,
                organizationId,
                "TEN-" + shortId(tenantId),
                "A12 Admission Integration Tenant"
        );

        jdbc.update(
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
                    ?, ?, 'PPIS', ?,
                    'UG', 'UGX', 'Africa/Kampala',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                schoolProfileId,
                tenantId,
                "Pio and Pretty International School",
                auditUser,
                auditUser
        );

        jdbc.update(
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
                    ?, ?, '2026', 'Academic Year 2026',
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    TRUE, 'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                academicYear2026Id,
                tenantId,
                auditUser,
                auditUser
        );

        jdbc.update(
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
                    ?, ?, '2027', 'Academic Year 2027',
                    DATE '2027-01-01',
                    DATE '2027-12-31',
                    FALSE, 'PLANNED',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?, 0
                )
                """,
                academicYear2027Id,
                tenantId,
                auditUser,
                auditUser
        );

        return new Fixture(
                tenantId,
                academicYear2026Id,
                academicYear2027Id
        );
    }

    private String shortId(
            UUID id
    ) {

        return id
                .toString()
                .substring(
                        0,
                        8
                )
                .toUpperCase();
    }

    private record Fixture(
            UUID tenantId,
            UUID academicYear2026Id,
            UUID academicYear2027Id
    ) {
    }
}
