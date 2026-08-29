package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class NotificationSecurePayloadPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_ens_secure_payload_test"
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
    JdbcTemplate jdbc;

    private UUID organizationId;
    private UUID tenantA;
    private UUID tenantB;
    private UUID notificationA;

    @BeforeEach
    void createFixture() {
        organizationId = UUID.randomUUID();
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        notificationA = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, ?)
                """,
                organizationId,
                "ENS-SECURE-" + shortId(organizationId),
                "ENS Secure Payload Test",
                sqlTimestamp(Instant.now())
        );

        createTenant(
                tenantA,
                "ENS-A-" + shortId(tenantA)
        );

        createTenant(
                tenantB,
                "ENS-B-" + shortId(tenantB)
        );

        createNotification(
                notificationA,
                tenantA
        );
    }

    @Test
    void validSecurePayloadPersists() {
        UUID payloadId = UUID.randomUUID();

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(900);

        insertPayload(
                payloadId,
                tenantA,
                notificationA,
                "FINAL_PROVIDER_BODY",
                "encrypted-value",
                "iv-value",
                "ens-test-key",
                expiresAt,
                null,
                createdAt
        );

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ens_notification_secure_payloads
                        WHERE tenant_id = ?
                          AND notification_request_id = ?
                          AND id = ?
                        """,
                        Integer.class,
                        tenantA,
                        notificationA,
                        payloadId
                );

        assertEquals(1, count);
    }

    @Test
    void securePayloadCannotCrossTenantBoundary() {
        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantB,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        "encrypted-value",
                        "iv-value",
                        "ens-test-key",
                        Instant.now().plusSeconds(900),
                        null,
                        Instant.now()
                )
        );
    }

    @Test
    void notificationCanHaveOnlyOneSecurePayload() {
        Instant createdAt = Instant.now();

        insertPayload(
                UUID.randomUUID(),
                tenantA,
                notificationA,
                "FINAL_PROVIDER_BODY",
                "encrypted-one",
                "iv-one",
                "ens-test-key",
                createdAt.plusSeconds(900),
                null,
                createdAt
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        "encrypted-two",
                        "iv-two",
                        "ens-test-key",
                        createdAt.plusSeconds(900),
                        null,
                        createdAt
                )
        );
    }

    @Test
    void payloadKindIsRestricted() {
        Instant createdAt = Instant.now();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "RAW_TOKEN",
                        "encrypted-value",
                        "iv-value",
                        "ens-test-key",
                        createdAt.plusSeconds(900),
                        null,
                        createdAt
                )
        );
    }

    @Test
    void encryptedFieldsCannotBeBlank() {
        Instant createdAt = Instant.now();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        " ",
                        "iv-value",
                        "ens-test-key",
                        createdAt.plusSeconds(900),
                        null,
                        createdAt
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        "encrypted-value",
                        " ",
                        "ens-test-key",
                        createdAt.plusSeconds(900),
                        null,
                        createdAt
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        "encrypted-value",
                        "iv-value",
                        " ",
                        createdAt.plusSeconds(900),
                        null,
                        createdAt
                )
        );
    }

    @Test
    void expiryMustBeAfterCreation() {
        Instant createdAt = Instant.now();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        "encrypted-value",
                        "iv-value",
                        "ens-test-key",
                        createdAt,
                        null,
                        createdAt
                )
        );
    }

    @Test
    void retirementCannotPrecedeCreation() {
        Instant createdAt = Instant.now();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertPayload(
                        UUID.randomUUID(),
                        tenantA,
                        notificationA,
                        "FINAL_PROVIDER_BODY",
                        "encrypted-value",
                        "iv-value",
                        "ens-test-key",
                        createdAt.plusSeconds(900),
                        createdAt.minusSeconds(1),
                        createdAt
                )
        );
    }

    private void createTenant(
            UUID tenantId,
            String code
    ) {
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
                VALUES (?, ?, ?, ?, 'ACTIVE', ?, 0)
                """,
                tenantId,
                organizationId,
                code,
                "ENS Secure Payload Tenant",
                sqlTimestamp(Instant.now())
        );
    }

    private void createNotification(
            UUID notificationId,
            UUID tenantId
    ) {
        Instant now = Instant.now();

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
                    body,
                    source_service,
                    attempt_count,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?
                )
                """,
                notificationId,
                tenantId,
                "ENS-SECURE-TEST",
                "guardian@example.com",
                "EMAIL",
                "NORMAL",
                "QUEUED",
                "Safe non-secret notification body",
                "ENS-TEST",
                0,
                sqlTimestamp(now),
                "ens-secure-payload-test",
                sqlTimestamp(now),
                "ens-secure-payload-test",
                0L,
                "ACTIVE"
        );
    }

    private void insertPayload(
            UUID payloadId,
            UUID tenantId,
            UUID notificationId,
            String payloadKind,
            String encryptedPayload,
            String iv,
            String keyId,
            Instant expiresAt,
            Instant retiredAt,
            Instant createdAt
    ) {
        jdbc.update(
                """
                INSERT INTO ens_notification_secure_payloads (
                    id,
                    tenant_id,
                    notification_request_id,
                    payload_kind,
                    encrypted_payload,
                    encryption_iv,
                    encryption_key_id,
                    expires_at,
                    retired_at,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE',
                    ?, ?, ?, ?, 0
                )
                """,
                payloadId,
                tenantId,
                notificationId,
                payloadKind,
                encryptedPayload,
                iv,
                keyId,
                sqlTimestamp(expiresAt),
                sqlTimestamp(retiredAt),
                sqlTimestamp(createdAt),
                "ens-secure-payload-test",
                sqlTimestamp(createdAt),
                "ens-secure-payload-test"
        );
    }

    private static Timestamp sqlTimestamp(
            Instant value
    ) {
        return value == null
                ? null
                : Timestamp.from(value);
    }

    private static String shortId(
            UUID id
    ) {
        return id.toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}
