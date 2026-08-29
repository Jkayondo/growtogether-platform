package africa.growtogether.platform.connect;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@Transactional
class ConnectMessageSearchPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_search_test"
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
    private JdbcTemplate jdbc;

    @Autowired
    private ConnectMessageRepository messages;

    @Test
    void v170CreatesGinConversationSearchIndex() {

        String indexDefinition =
                jdbc.queryForObject(
                        """
                        SELECT indexdef
                        FROM pg_indexes
                        WHERE schemaname = 'public'
                          AND indexname =
                              'ix_gt_connect_messages_body_search'
                        """,
                        String.class
                );

        assertNotNull(
                indexDefinition
        );

        assertTrue(
                indexDefinition
                        .toLowerCase()
                        .contains(
                                "using gin"
                        )
        );
    }

    @Test
    void findsRelevantMessageWithinTenantAndSpace() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                insertSpace(
                        tenantId
                );

        UUID expectedMessageId =
                insertMessage(
                        tenantId,
                        spaceId,
                        "School fees payment deadline is Friday",
                        false
                );

        insertMessage(
                tenantId,
                spaceId,
                "Sports training begins tomorrow",
                false
        );

        List<ConnectMessage> found =
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "school fees",
                        25
                );

        assertEquals(
                1,
                found.size()
        );

        assertEquals(
                expectedMessageId,
                found.get(0).getId()
        );
    }

    @Test
    void searchCannotCrossTenantBoundary() {

        UUID firstTenant =
                UUID.randomUUID();

        UUID secondTenant =
                UUID.randomUUID();

        UUID firstSpace =
                insertSpace(
                        firstTenant
                );

        UUID secondSpace =
                insertSpace(
                        secondTenant
                );

        UUID expectedMessageId =
                insertMessage(
                        firstTenant,
                        firstSpace,
                        "Parents meeting on Saturday",
                        false
                );

        insertMessage(
                secondTenant,
                secondSpace,
                "Parents meeting on Saturday",
                false
        );

        List<ConnectMessage> found =
                messages.searchConversation(
                        firstTenant,
                        firstSpace,
                        "parents meeting",
                        25
                );

        assertEquals(
                1,
                found.size()
        );

        assertEquals(
                expectedMessageId,
                found.get(0).getId()
        );

        assertEquals(
                firstTenant,
                found.get(0).getTenantId()
        );
    }

    @Test
    void searchCannotCrossSpaceBoundary() {

        UUID tenantId =
                UUID.randomUUID();

        UUID firstSpace =
                insertSpace(
                        tenantId
                );

        UUID secondSpace =
                insertSpace(
                        tenantId
                );

        UUID expectedMessageId =
                insertMessage(
                        tenantId,
                        firstSpace,
                        "Mathematics homework exercise",
                        false
                );

        insertMessage(
                tenantId,
                secondSpace,
                "Mathematics homework exercise",
                false
        );

        List<ConnectMessage> found =
                messages.searchConversation(
                        tenantId,
                        firstSpace,
                        "mathematics homework",
                        25
                );

        assertEquals(
                1,
                found.size()
        );

        assertEquals(
                expectedMessageId,
                found.get(0).getId()
        );

        assertEquals(
                firstSpace,
                found.get(0).getSpaceId()
        );
    }

    @Test
    void deletedMessagesAreExcludedFromSearch() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                insertSpace(
                        tenantId
                );

        insertMessage(
                tenantId,
                spaceId,
                "Confidential trip information",
                true
        );

        List<ConnectMessage> found =
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "confidential trip",
                        25
                );

        assertTrue(
                found.isEmpty()
        );
    }

    @Test
    void requestedResultLimitIsEnforcedByDatabaseQuery() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                insertSpace(
                        tenantId
                );

        insertMessage(
                tenantId,
                spaceId,
                "Meeting agenda number one",
                false
        );

        insertMessage(
                tenantId,
                spaceId,
                "Meeting agenda number two",
                false
        );

        insertMessage(
                tenantId,
                spaceId,
                "Meeting agenda number three",
                false
        );

        List<ConnectMessage> found =
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "meeting agenda",
                        1
                );

        assertEquals(
                1,
                found.size()
        );
    }

    @Test
    void searchReflectsCurrentEditedMessageBody() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                insertSpace(
                        tenantId
                );

        UUID messageId =
                insertMessage(
                        tenantId,
                        spaceId,
                        "Old timetable information",
                        false
                );

        jdbc.update(
                """
                UPDATE gt_connect_messages
                SET body = ?,
                    edited_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND tenant_id = ?
                """,
                "Updated examination timetable",
                messageId,
                tenantId
        );

        List<ConnectMessage> oldSearch =
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "old timetable",
                        25
                );

        assertTrue(
                oldSearch.isEmpty()
        );

        List<ConnectMessage> newSearch =
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "examination timetable",
                        25
                );

        assertEquals(
                1,
                newSearch.size()
        );

        assertEquals(
                messageId,
                newSearch.get(0).getId()
        );
    }

    private UUID insertSpace(
            UUID tenantId
    ) {

        UUID spaceId =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gt_connect_spaces (
                    id,
                    tenant_id,
                    space_type,
                    name,
                    context_type,
                    context_reference,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    'GROUP',
                    'Search Test Space',
                    NULL,
                    NULL,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                spaceId,
                tenantId
        );

        return spaceId;
    }

    private UUID insertMessage(
            UUID tenantId,
            UUID spaceId,
            String body,
            boolean deleted
    ) {

        UUID messageId =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gt_connect_messages (
                    id,
                    tenant_id,
                    space_id,
                    sender_user_id,
                    message_type,
                    body,
                    reply_to_message_id,
                    sent_at,
                    edited_at,
                    deleted_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    'TEXT',
                    ?,
                    NULL,
                    CURRENT_TIMESTAMP,
                    NULL,
                    CASE
                        WHEN ? THEN CURRENT_TIMESTAMP
                        ELSE NULL
                    END,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                messageId,
                tenantId,
                spaceId,
                UUID.randomUUID(),
                body,
                deleted
        );

        return messageId;
    }
}
