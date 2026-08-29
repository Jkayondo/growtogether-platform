package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class NotificationProviderAttemptAllocatorPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_ens_test"
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
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private NotificationProviderAttemptAllocator allocator;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void concurrentAllocationsReceiveSequentialAttemptNumbers()
            throws Exception {

        Fixture fixture =
                createFixture();

        CountDownLatch firstAttemptAllocated =
                new CountDownLatch(1);

        CountDownLatch allowFirstCommit =
                new CountDownLatch(1);

        CountDownLatch secondAboutToAllocate =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first =
                    executor.submit(
                            () -> allocateInTransaction(
                                    fixture,
                                    null,
                                    () -> {
                                        firstAttemptAllocated.countDown();
                                        await(allowFirstCommit);
                                    }
                            )
                    );

            firstAttemptAllocated.await();

            Future<Integer> second =
                    executor.submit(
                            () -> allocateInTransaction(
                                    fixture,
                                    secondAboutToAllocate::countDown,
                                    null
                            )
                    );

            secondAboutToAllocate.await();

            assertFalse(
                    second.isDone(),
                    "Second provider-attempt allocation must wait "
                            + "for the notification row lock"
            );

            allowFirstCommit.countDown();

            int firstNumber =
                    first.get();

            int secondNumber =
                    second.get();

            assertEquals(1, firstNumber);
            assertEquals(2, secondNumber);

            List<Integer> databaseNumbers =
                    jdbc.queryForList(
                            """
                            SELECT attempt_number
                            FROM ens_notification_provider_attempts
                            WHERE tenant_id = ?
                              AND notification_request_id = ?
                            ORDER BY attempt_number
                            """,
                            Integer.class,
                            fixture.tenantId(),
                            fixture.notificationId()
                    );

            assertEquals(
                    List.of(1, 2),
                    databaseNumbers
            );

            Integer attemptCount =
                    jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM ens_notification_provider_attempts
                            WHERE tenant_id = ?
                              AND notification_request_id = ?
                            """,
                            Integer.class,
                            fixture.tenantId(),
                            fixture.notificationId()
                    );

            assertEquals(
                    2,
                    attemptCount
            );

        } finally {
            allowFirstCommit.countDown();
            executor.shutdownNow();
        }
    }

    private int allocateInTransaction(
            Fixture fixture,
            Runnable beforeAllocation,
            Runnable beforeCommit
    ) {
        RequestContextHolder.set(
                new RequestContext(
                        "ens-attempt-concurrency",
                        fixture.tenantId().toString()
                )
        );

        try {
            TransactionTemplate transaction =
                    new TransactionTemplate(
                            transactionManager
                    );

            Integer result =
                    transaction.execute(
                            status -> {
                                if (beforeAllocation != null) {
                                    beforeAllocation.run();
                                }

                                NotificationProviderAttempt attempt =
                                        allocator.allocate(
                                                fixture.tenantId(),
                                                fixture.notificationId(),
                                                fixture.connectorId(),
                                                fixture.routeId()
                                        );

                                if (beforeCommit != null) {
                                    beforeCommit.run();
                                }

                                return attempt.attemptNumber();
                            }
                    );

            if (result == null) {
                throw new IllegalStateException(
                        "Provider-attempt transaction returned no result"
                );
            }

            return result;

        } finally {
            RequestContextHolder.clear();
        }
    }

    private Fixture createFixture() {
        UUID tenantId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();

        String auditUser =
                "ens-provider-attempt-concurrency-test";

        jdbc.update(
                """
                INSERT INTO ens_notification_requests (
                    id,
                    tenant_id,
                    definition_code,
                    recipient,
                    channel,
                    priority,
                    notification_status,
                    subject,
                    body,
                    correlation_id,
                    source_service,
                    source_reference,
                    attempt_count,
                    next_attempt_at,
                    provider_reference,
                    last_error,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'TEST_NOTIFICATION',
                    '+256700000000',
                    'WHATSAPP',
                    'NORMAL',
                    'QUEUED',
                    'Concurrency Test',
                    'Provider attempt allocation test',
                    'ens-attempt-concurrency',
                    'ENS_TEST',
                    'CONCURRENCY',
                    0,
                    NULL,
                    NULL,
                    NULL,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                notificationId,
                tenantId,
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO eip_external_connectors (
                    id,
                    tenant_id,
                    connector_code,
                    connector_type,
                    base_url,
                    auth_type,
                    credential_ciphertext,
                    credential_key_id,
                    active,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'ENS-CONCURRENCY-CONNECTOR',
                    'WHATSAPP',
                    'https://provider.example.test',
                    'BEARER',
                    NULL,
                    NULL,
                    TRUE,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                connectorId,
                tenantId,
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO ens_notification_channel_routes (
                    id,
                    tenant_id,
                    channel,
                    connector_id,
                    priority,
                    enabled,
                    failover_enabled,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'WHATSAPP',
                    ?,
                    1,
                    TRUE,
                    TRUE,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                routeId,
                tenantId,
                connectorId,
                auditUser,
                auditUser
        );

        return new Fixture(
                tenantId,
                notificationId,
                connectorId,
                routeId
        );
    }

    private void await(
            CountDownLatch latch
    ) {
        try {
            latch.await();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Concurrency test interrupted",
                    exception
            );
        }
    }

    private record Fixture(
            UUID tenantId,
            UUID notificationId,
            UUID connectorId,
            UUID routeId
    ) {
    }
}
