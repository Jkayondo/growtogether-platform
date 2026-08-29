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
class ConnectSystemMessagePostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_system_message_test"
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
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void flywayAppliesSystemProvenanceMigrationAndAutomatedMessagePersists() {

        Fixture fixture =
                fixture();

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '166'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );

        ConnectMessage message =
                ConnectMessage.automatedSystem(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        "Automated school reminder.",
                        "GT-SCHOOL",
                        "calendar-event-001"
                );

        ConnectMessage saved =
                messages.saveAndFlush(
                        message
                );

        assertNotNull(
                saved.getId()
        );

        assertNull(
                saved.getSenderUserId()
        );

        assertEquals(
                ConnectMessageType.SYSTEM,
                saved.getMessageType()
        );

        assertEquals(
                "GT-SCHOOL",
                saved.getSourceService()
        );

        assertEquals(
                "calendar-event-001",
                saved.getSourceReference()
        );
    }

    @Test
    void automatedSystemMessageRequiresSourceService() {

        Fixture fixture =
                fixture();

        assertThrows(
                IllegalArgumentException.class,
                () -> ConnectMessage.automatedSystem(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        "Automated message",
                        "   ",
                        null
                )
        );
    }

    @Test
    void automatedSystemMessageRequiresBody() {

        Fixture fixture =
                fixture();

        assertThrows(
                IllegalArgumentException.class,
                () -> ConnectMessage.automatedSystem(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        "   ",
                        "GT-SCHOOL",
                        null
                )
        );
    }

    @Test
    void databaseRejectsHumanMessageWithoutSender() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        """
                        INSERT INTO gt_connect_messages (
                            id,
                            tenant_id,
                            space_id,
                            sender_user_id,
                            message_type,
                            body,
                            sent_at,
                            source_service,
                            source_reference,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version,
                            status
                        )
                        VALUES (
                            ?, ?, ?,
                            NULL,
                            'TEXT',
                            'Human messages require a sender.',
                            CURRENT_TIMESTAMP,
                            NULL,
                            NULL,
                            CURRENT_TIMESTAMP,
                            'system-message-db-test',
                            CURRENT_TIMESTAMP,
                            'system-message-db-test',
                            0,
                            'ACTIVE'
                        )
                        """,
                        UUID.randomUUID(),
                        fixture.tenantId(),
                        fixture.spaceId()
                )
        );
    }

    @Test
    void databaseRejectsSenderlessSystemMessageWithoutSourceService() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        """
                        INSERT INTO gt_connect_messages (
                            id,
                            tenant_id,
                            space_id,
                            sender_user_id,
                            message_type,
                            body,
                            sent_at,
                            source_service,
                            source_reference,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version,
                            status
                        )
                        VALUES (
                            ?, ?, ?,
                            NULL,
                            'SYSTEM',
                            'Automated message without provenance.',
                            CURRENT_TIMESTAMP,
                            NULL,
                            NULL,
                            CURRENT_TIMESTAMP,
                            'system-message-db-test',
                            CURRENT_TIMESTAMP,
                            'system-message-db-test',
                            0,
                            'ACTIVE'
                        )
                        """,
                        UUID.randomUUID(),
                        fixture.tenantId(),
                        fixture.spaceId()
                )
        );
    }

    @Test
    void ordinaryHumanTextMessageStillPersists() {

        Fixture fixture =
                fixture();

        UUID senderUserId =
                UUID.randomUUID();

        ConnectMessage message =
                new ConnectMessage(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        senderUserId,
                        ConnectMessageType.TEXT,
                        "Normal human message.",
                        null
                );

        ConnectMessage saved =
                messages.saveAndFlush(
                        message
                );

        assertNotNull(
                saved.getId()
        );

        assertEquals(
                senderUserId,
                saved.getSenderUserId()
        );

        assertEquals(
                ConnectMessageType.TEXT,
                saved.getMessageType()
        );

        assertNull(
                saved.getSourceService()
        );
    }

    private Fixture fixture() {

        UUID tenantId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "gt-connect-system-message-test",
                        tenantId.toString()
                )
        );

        ConnectSpace space =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                tenantId,
                                ConnectSpaceType.GROUP,
                                "System Message Test Space",
                                null,
                                null
                        )
                );

        return new Fixture(
                tenantId,
                space.getId()
        );
    }

    private record Fixture(
            UUID tenantId,
            UUID spaceId
    ) {
    }
}
