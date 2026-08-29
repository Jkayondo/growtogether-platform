package africa.growtogether.platform.connect;

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
class ConnectAttachmentPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_attachment_test"
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

    @Test
    void v169MigrationCreatesAttachmentTable() {

        String table =
                jdbc.queryForObject(
                        """
                        SELECT to_regclass(
                            'public.gt_connect_message_attachments'
                        )::text
                        """,
                        String.class
                );

        assertEquals(
                "gt_connect_message_attachments",
                table
        );
    }

    @Test
    void sameTenantExactDocumentVersionCanBeAttached() {

        Fixture fixture =
                fixture();

        UUID attachmentId =
                UUID.randomUUID();

        insertAttachment(
                attachmentId,
                fixture.tenantId(),
                fixture.messageId(),
                fixture.documentId(),
                1
        );

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gt_connect_message_attachments
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                        Integer.class,
                        attachmentId,
                        fixture.tenantId()
                );

        assertEquals(
                1,
                count
        );
    }

    @Test
    void crossTenantMessageReferenceIsRejected() {

        Fixture first =
                fixture();

        Fixture second =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertAttachment(
                                UUID.randomUUID(),
                                second.tenantId(),
                                first.messageId(),
                                second.documentId(),
                                1
                        )
        );
    }

    @Test
    void crossTenantEdsDocumentVersionReferenceIsRejected() {

        Fixture first =
                fixture();

        Fixture second =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertAttachment(
                                UUID.randomUUID(),
                                first.tenantId(),
                                first.messageId(),
                                second.documentId(),
                                1
                        )
        );
    }

    @Test
    void nonexistentHistoricalVersionIsRejected() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertAttachment(
                                UUID.randomUUID(),
                                fixture.tenantId(),
                                fixture.messageId(),
                                fixture.documentId(),
                                99
                        )
        );
    }

    @Test
    void duplicateExactAttachmentReferenceIsRejected() {

        Fixture fixture =
                fixture();

        insertAttachment(
                UUID.randomUUID(),
                fixture.tenantId(),
                fixture.messageId(),
                fixture.documentId(),
                1
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertAttachment(
                                UUID.randomUUID(),
                                fixture.tenantId(),
                                fixture.messageId(),
                                fixture.documentId(),
                                1
                        )
        );
    }

    @Test
    void nonPositiveDocumentVersionIsRejected() {

        Fixture fixture =
                fixture();

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertAttachment(
                                UUID.randomUUID(),
                                fixture.tenantId(),
                                fixture.messageId(),
                                fixture.documentId(),
                                0
                        )
        );
    }

    private Fixture fixture() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        UUID documentVersionId =
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
                    ?, ?, 'GROUP', 'Attachment Test',
                    NULL, NULL,
                    CURRENT_TIMESTAMP, 'test',
                    CURRENT_TIMESTAMP, 'test',
                    0, 'ACTIVE'
                )
                """,
                spaceId,
                tenantId
        );

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
                    ?, ?, ?, ?,
                    'FILE',
                    'Attachment test',
                    NULL,
                    CURRENT_TIMESTAMP,
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
                messageId,
                tenantId,
                spaceId,
                UUID.randomUUID()
        );

        jdbc.update(
                """
                INSERT INTO eds_documents (
                    id,
                    tenant_id,
                    document_number,
                    title,
                    description,
                    document_status,
                    classification,
                    current_version,
                    retention_until,
                    legal_hold,
                    checked_out_by,
                    checked_out_at,
                    archived_at,
                    deleted_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    ?,
                    'GT Connect Attachment Test',
                    NULL,
                    'ACTIVE',
                    'INTERNAL',
                    1,
                    NULL,
                    FALSE,
                    NULL,
                    NULL,
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
                documentId,
                tenantId,
                "GT-EDS-" + UUID.randomUUID()
        );

        jdbc.update(
                """
                INSERT INTO eds_document_versions (
                    id,
                    tenant_id,
                    document_id,
                    version_number,
                    storage_key,
                    checksum,
                    mime_type,
                    size_bytes,
                    change_summary,
                    immutable,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, 1,
                    ?,
                    ?,
                    'application/pdf',
                    4096,
                    'Initial attachment version',
                    TRUE,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                documentVersionId,
                tenantId,
                documentId,
                "connect-test/" + UUID.randomUUID(),
                UUID.randomUUID().toString()
        );

        return new Fixture(
                tenantId,
                spaceId,
                messageId,
                documentId
        );
    }

    private void insertAttachment(
            UUID attachmentId,
            UUID tenantId,
            UUID messageId,
            UUID documentId,
            int documentVersion
    ) {

        jdbc.update(
                """
                INSERT INTO gt_connect_message_attachments (
                    id,
                    tenant_id,
                    message_id,
                    document_id,
                    document_version,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                attachmentId,
                tenantId,
                messageId,
                documentId,
                documentVersion
        );
    }

    private record Fixture(
            UUID tenantId,
            UUID spaceId,
            UUID messageId,
            UUID documentId
    ) {
    }
}
