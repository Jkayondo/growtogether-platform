package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eip.ExternalProviderDispatchRequest;
import africa.growtogether.platform.eip.ExternalProviderDispatchResult;
import africa.growtogether.platform.eip.ExternalProviderExecutionGateway;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class NotificationTransactionBoundaryPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_ens_tx_test"
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
    private NotificationProviderAttemptEvidenceService evidence;

    @Autowired
    private NotificationExternalProviderExecutionService execution;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private ExternalProviderExecutionGateway gateway;

    @Test
    void allocationRequiresNewSurvivesOuterRollback() {
        Fixture fixture =
                createFixture();

        withContext(
                fixture,
                () -> {
                    TransactionTemplate outer =
                            new TransactionTemplate(
                                    transactionManager
                            );

                    outer.executeWithoutResult(
                            status -> {
                                assertTrue(
                                        TransactionSynchronizationManager
                                                .isActualTransactionActive(),
                                        "Outer transaction must be active"
                                );

                                evidence.allocate(
                                        fixture.tenantId(),
                                        fixture.notificationId(),
                                        fixture.connectorId(),
                                        fixture.routeId()
                                );

                                status.setRollbackOnly();
                            }
                    );
                }
        );

        Integer count =
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
                1,
                count,
                "REQUIRES_NEW allocation must survive outer rollback"
        );
    }

    @Test
    void resultEvidenceRequiresNewSurvivesOuterRollback() {
        Fixture fixture =
                createFixture();

        final UUID[] attemptId =
                new UUID[1];

        withContext(
                fixture,
                () -> {
                    NotificationProviderAttempt allocated =
                            evidence.allocate(
                                    fixture.tenantId(),
                                    fixture.notificationId(),
                                    fixture.connectorId(),
                                    fixture.routeId()
                            );

                    attemptId[0] =
                            allocated.id();

                    TransactionTemplate outer =
                            new TransactionTemplate(
                                    transactionManager
                            );

                    outer.executeWithoutResult(
                            status -> {
                                assertTrue(
                                        TransactionSynchronizationManager
                                                .isActualTransactionActive(),
                                        "Outer transaction must be active"
                                );

                                evidence.recordResult(
                                        fixture.tenantId(),
                                        attemptId[0],
                                        new ExternalProviderDispatchResult(
                                                ExternalProviderDispatchResult.Status.ACCEPTED,
                                                "provider-request-1",
                                                "provider-reference-1",
                                                "200",
                                                "Accepted"
                                        )
                                );

                                status.setRollbackOnly();
                            }
                    );
                }
        );

        String attemptStatus =
                jdbc.queryForObject(
                        """
                        SELECT attempt_status
                        FROM ens_notification_provider_attempts
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        String.class,
                        fixture.tenantId(),
                        attemptId[0]
                );

        String providerReference =
                jdbc.queryForObject(
                        """
                        SELECT provider_reference
                        FROM ens_notification_provider_attempts
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        String.class,
                        fixture.tenantId(),
                        attemptId[0]
                );

        assertEquals(
                "ACCEPTED",
                attemptStatus
        );

        assertEquals(
                "provider-reference-1",
                providerReference,
                "REQUIRES_NEW result evidence must survive outer rollback"
        );
    }

    @Test
    void externalExecutionSuspendsActiveTransaction() {
        Fixture fixture =
                createFixture();

        AtomicBoolean outerTransactionActive =
                new AtomicBoolean(false);

        AtomicBoolean transactionActiveInsideGateway =
                new AtomicBoolean(true);

        ExternalProviderDispatchRequest request =
                new ExternalProviderDispatchRequest(
                        "WHATSAPP",
                        "+256700000000",
                        "Transaction Test",
                        "External execution must not hold a DB transaction",
                        "ens-tx-boundary",
                        "ens-notification:"
                                + fixture.notificationId()
                                + ":connector:"
                                + fixture.connectorId(),
                        Map.of()
                );

        when(
                gateway.dispatch(
                        eq(fixture.tenantId()),
                        eq(fixture.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenAnswer(
                invocation -> {
                    transactionActiveInsideGateway.set(
                            TransactionSynchronizationManager
                                    .isActualTransactionActive()
                    );

                    return new ExternalProviderDispatchResult(
                            ExternalProviderDispatchResult.Status.ACCEPTED,
                            "provider-request-tx",
                            "provider-reference-tx",
                            "200",
                            "Accepted"
                    );
                }
        );

        withContext(
                fixture,
                () -> {
                    TransactionTemplate outer =
                            new TransactionTemplate(
                                    transactionManager
                            );

                    outer.executeWithoutResult(
                            status -> {
                                outerTransactionActive.set(
                                        TransactionSynchronizationManager
                                                .isActualTransactionActive()
                                );

                                ExternalProviderDispatchResult result =
                                        execution.dispatch(
                                                fixture.tenantId(),
                                                fixture.connectorId(),
                                                request
                                        );

                                assertEquals(
                                        ExternalProviderDispatchResult.Status.ACCEPTED,
                                        result.status()
                                );

                                status.setRollbackOnly();
                            }
                    );
                }
        );

        assertTrue(
                outerTransactionActive.get(),
                "The caller must begin with an active transaction"
        );

        assertFalse(
                transactionActiveInsideGateway.get(),
                "NOT_SUPPORTED must suspend the transaction "
                        + "during external provider execution"
        );
    }

    private Fixture createFixture() {
        UUID tenantId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        UUID connectorId =
                UUID.randomUUID();

        UUID routeId =
                UUID.randomUUID();

        String auditUser =
                "ens-transaction-boundary-test";

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
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'TX_BOUNDARY_TEST',
                    '+256700000000',
                    'WHATSAPP',
                    'NORMAL',
                    'PROCESSING',
                    'Transaction Boundary Test',
                    'ENS transaction boundary verification',
                    'ens-tx-boundary',
                    'ENS_TEST',
                    'TX-BOUNDARY',
                    1,
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
                    'ENS-TX-CONNECTOR',
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

    private void withContext(
            Fixture fixture,
            Runnable work
    ) {
        RequestContext previous =
                RequestContextHolder.current()
                        .orElse(null);

        RequestContextHolder.set(
                new RequestContext(
                        "ens-tx-boundary",
                        fixture.tenantId().toString()
                )
        );

        try {
            work.run();

        } finally {
            if (previous == null) {
                RequestContextHolder.clear();

            } else {
                RequestContextHolder.set(
                        previous
                );
            }
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
