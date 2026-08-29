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
class ConnectMessageReceiptPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_receipt_test"
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
    private ConnectMessageRepository messages;

    @Autowired
    private ConnectMessageReceiptRepository receipts;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void flywayAppliesReceiptMigrationAndReceiptPersists() {

        Fixture fixture =
                fixture();

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '154'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );

        ConnectMessageReceipt receipt =
                new ConnectMessageReceipt(
                        fixture.tenantId(),
                        fixture.messageId(),
                        UUID.randomUUID()
                );

        receipt.markDelivered();

        ConnectMessageReceipt saved =
                receipts.saveAndFlush(
                        receipt
                );

        assertNotNull(
                saved.getId()
        );

        assertNotNull(
                saved.getDeliveredAt()
        );

        assertNull(
                saved.getReadAt()
        );
    }

    @Test
    void databaseRejectsDuplicateReceiptForSameUserAndMessage() {

        Fixture fixture =
                fixture();

        UUID userId =
                UUID.randomUUID();

        ConnectMessageReceipt first =
                new ConnectMessageReceipt(
                        fixture.tenantId(),
                        fixture.messageId(),
                        userId
                );

        first.markDelivered();

        receipts.saveAndFlush(
                first
        );

        ConnectMessageReceipt duplicate =
                new ConnectMessageReceipt(
                        fixture.tenantId(),
                        fixture.messageId(),
                        userId
                );

        duplicate.markDelivered();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> receipts.saveAndFlush(
                        duplicate
                )
        );
    }

    @Test
    void databaseRejectsReadWithoutDelivered() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        """
                        INSERT INTO gt_connect_message_receipts (
                            id,
                            tenant_id,
                            message_id,
                            user_id,
                            delivered_at,
                            read_at,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version
                        )
                        VALUES (
                            ?, ?, ?, ?,
                            NULL,
                            CURRENT_TIMESTAMP,
                            'ACTIVE',
                            CURRENT_TIMESTAMP,
                            'receipt-db-test',
                            CURRENT_TIMESTAMP,
                            'receipt-db-test',
                            0
                        )
                        """,
                        UUID.randomUUID(),
                        fixture.tenantId(),
                        fixture.messageId(),
                        UUID.randomUUID()
                )
        );
    }

    @Test
    void databaseRejectsCrossTenantMessageReceipt() {

        Fixture fixture =
                fixture();

        UUID otherTenantId =
                UUID.randomUUID();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        """
                        INSERT INTO gt_connect_message_receipts (
                            id,
                            tenant_id,
                            message_id,
                            user_id,
                            delivered_at,
                            read_at,
                            status,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version
                        )
                        VALUES (
                            ?, ?, ?, ?,
                            CURRENT_TIMESTAMP,
                            NULL,
                            'ACTIVE',
                            CURRENT_TIMESTAMP,
                            'receipt-db-test',
                            CURRENT_TIMESTAMP,
                            'receipt-db-test',
                            0
                        )
                        """,
                        UUID.randomUUID(),
                        otherTenantId,
                        fixture.messageId(),
                        UUID.randomUUID()
                )
        );
    }

    private Fixture fixture() {

        UUID tenantId =
                UUID.randomUUID();

        UUID senderUserId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "gt-connect-receipt-test",
                        tenantId.toString()
                )
        );

        ConnectSpace space =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                tenantId,
                                ConnectSpaceType.GROUP,
                                "Receipt Test Space",
                                null,
                                null
                        )
                );

        ConnectMessage message =
                messages.saveAndFlush(
                        new ConnectMessage(
                                tenantId,
                                space.getId(),
                                senderUserId,
                                ConnectMessageType.TEXT,
                                "Receipt integration test",
                                null
                        )
                );

        return new Fixture(
                tenantId,
                space.getId(),
                message.getId()
        );
    }

    private record Fixture(
            UUID tenantId,
            UUID spaceId,
            UUID messageId
    ) {
    }
}
