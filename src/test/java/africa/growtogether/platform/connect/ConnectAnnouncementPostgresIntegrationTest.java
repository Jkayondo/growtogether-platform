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
class ConnectAnnouncementPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_announcement_test"
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
    private ConnectAnnouncementRepository announcements;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void flywayAppliesAnnouncementMigrationAndAnnouncementPersists() {

        Fixture fixture =
                fixture();

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '158'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );

        ConnectAnnouncement announcement =
                new ConnectAnnouncement(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        fixture.messageId(),
                        fixture.publisherUserId(),
                        "School Assembly",
                        "All learners report to the assembly area."
                );

        ConnectAnnouncement saved =
                announcements.saveAndFlush(
                        announcement
                );

        assertNotNull(
                saved.getId()
        );

        assertEquals(
                fixture.spaceId(),
                saved.getSpaceId()
        );

        assertEquals(
                fixture.messageId(),
                saved.getMessageId()
        );

        assertEquals(
                fixture.publisherUserId(),
                saved.getPublishedByUserId()
        );

        assertNotNull(
                saved.getPublishedAt()
        );
    }

    @Test
    void databaseRejectsSecondAnnouncementForSameMessage() {

        Fixture fixture =
                fixture();

        ConnectAnnouncement first =
                new ConnectAnnouncement(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        fixture.messageId(),
                        fixture.publisherUserId(),
                        "First Announcement",
                        "First official publication."
                );

        announcements.saveAndFlush(
                first
        );

        ConnectAnnouncement duplicate =
                new ConnectAnnouncement(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        fixture.messageId(),
                        fixture.publisherUserId(),
                        "Duplicate Announcement",
                        "This message must not back two announcements."
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> announcements.saveAndFlush(
                        duplicate
                )
        );
    }

    @Test
    void databaseRejectsAnnouncementUsingMessageFromAnotherTenant() {

        Fixture fixture =
                fixture();

        UUID otherTenantId =
                UUID.randomUUID();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertAnnouncement(
                        otherTenantId,
                        fixture.spaceId(),
                        fixture.messageId(),
                        UUID.randomUUID(),
                        "Cross Tenant",
                        "Must be rejected."
                )
        );
    }

    @Test
    void databaseRejectsAnnouncementUsingMessageFromAnotherSpace() {

        Fixture fixture =
                fixture();

        ConnectSpace otherSpace =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                fixture.tenantId(),
                                ConnectSpaceType.GROUP,
                                "Different Audience",
                                null,
                                null
                        )
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertAnnouncement(
                        fixture.tenantId(),
                        otherSpace.getId(),
                        fixture.messageId(),
                        fixture.publisherUserId(),
                        "Wrong Space",
                        "Must be rejected."
                )
        );
    }

    @Test
    void databaseRejectsBlankAnnouncementTitle() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertAnnouncement(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        fixture.messageId(),
                        fixture.publisherUserId(),
                        "   ",
                        "Valid body."
                )
        );
    }

    @Test
    void databaseRejectsBlankAnnouncementBody() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertAnnouncement(
                        fixture.tenantId(),
                        fixture.spaceId(),
                        fixture.messageId(),
                        fixture.publisherUserId(),
                        "Valid title",
                        "   "
                )
        );
    }

    private void insertAnnouncement(
            UUID tenantId,
            UUID spaceId,
            UUID messageId,
            UUID publisherUserId,
            String title,
            String body
    ) {

        jdbc.update(
                """
                INSERT INTO gt_connect_announcements (
                    id,
                    tenant_id,
                    space_id,
                    message_id,
                    published_by_user_id,
                    title,
                    body,
                    published_at,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?,
                    CURRENT_TIMESTAMP,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'announcement-db-test',
                    CURRENT_TIMESTAMP,
                    'announcement-db-test',
                    0
                )
                """,
                UUID.randomUUID(),
                tenantId,
                spaceId,
                messageId,
                publisherUserId,
                title,
                body
        );
    }

    private Fixture fixture() {

        UUID tenantId =
                UUID.randomUUID();

        UUID publisherUserId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "gt-connect-announcement-test",
                        tenantId.toString()
                )
        );

        ConnectSpace space =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                tenantId,
                                ConnectSpaceType.GROUP,
                                "Announcement Test Audience",
                                null,
                                null
                        )
                );

        ConnectMessage message =
                messages.saveAndFlush(
                        new ConnectMessage(
                                tenantId,
                                space.getId(),
                                publisherUserId,
                                ConnectMessageType.SYSTEM,
                                "Official announcement projection",
                                null
                        )
                );

        return new Fixture(
                tenantId,
                space.getId(),
                message.getId(),
                publisherUserId
        );
    }

    private record Fixture(
            UUID tenantId,
            UUID spaceId,
            UUID messageId,
            UUID publisherUserId
    ) {
    }
}
