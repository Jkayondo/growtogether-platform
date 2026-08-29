package africa.growtogether.platform.school.guardian;

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
class GuardianNumberPostgresIntegrationTest {

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO gts_guardian_number_sequence (
                tenant_id,
                last_issued_number
            )
            VALUES (?, 1)
            ON CONFLICT (tenant_id)
            DO UPDATE
            SET last_issued_number =
                    gts_guardian_number_sequence.last_issued_number + 1,
                updated_at = CURRENT_TIMESTAMP
            RETURNING last_issued_number
            """;

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_guardian_test"
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
    void incrementsPermanentlyWithinTenantAndSeparatesTenants() {

        UUID firstTenant =
                createTenant(
                        "Guardian Number Tenant One"
                );

        UUID secondTenant =
                createTenant(
                        "Guardian Number Tenant Two"
                );

        assertEquals(
                1L,
                nextSequence(
                        firstTenant
                )
        );

        assertEquals(
                2L,
                nextSequence(
                        firstTenant
                )
        );

        assertEquals(
                3L,
                nextSequence(
                        firstTenant
                )
        );

        assertEquals(
                1L,
                nextSequence(
                        secondTenant
                )
        );

        assertEquals(
                2L,
                nextSequence(
                        secondTenant
                )
        );
    }

    @Test
    void concurrentRequestsReceiveDistinctGuardianSequenceNumbers()
            throws Exception {

        UUID tenantId =
                createTenant(
                        "Concurrent Guardian Number Tenant"
                );

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
                                            tenantId
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
                    "Concurrent guardian requests must receive unique numbers"
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
                        "Missing guardian sequence number: "
                                + expected
                );
            }

            Long databaseValue =
                    jdbc.queryForObject(
                            """
                            SELECT last_issued_number
                            FROM gts_guardian_number_sequence
                            WHERE tenant_id = ?
                            """,
                            Long.class,
                            tenantId
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
            UUID tenantId
    ) {

        Long sequence =
                jdbc.queryForObject(
                        NEXT_SEQUENCE_SQL,
                        Long.class,
                        tenantId
                );

        if (sequence == null) {
            throw new IllegalStateException(
                    "Guardian sequence was not returned"
            );
        }

        return sequence;
    }

    private UUID createTenant(
            String name
    ) {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

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
                "ORG-" + shortId(
                        organizationId
                ),
                name + " Organisation"
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
                    ?, ?, ?, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                "TEN-" + shortId(
                        tenantId
                ),
                name
        );

        return tenantId;
    }

    private static String shortId(
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
}
