package africa.growtogether.platform.connect;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectMessageTest {

    @Test
    void createsTextMessage() {
        UUID tenantId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        ConnectMessage message =
                new ConnectMessage(
                        tenantId,
                        spaceId,
                        senderId,
                        ConnectMessageType.TEXT,
                        " Hello GT Connect ",
                        null
                );

        assertEquals(tenantId, message.getTenantId());
        assertEquals(spaceId, message.getSpaceId());
        assertEquals(senderId, message.getSenderUserId());

        assertEquals(
                ConnectMessageType.TEXT,
                message.getMessageType()
        );

        assertEquals(
                "Hello GT Connect",
                message.getBody()
        );

        assertNotNull(message.getSentAt());
        assertNull(message.getEditedAt());
        assertNull(message.getDeletedAt());
    }

    @Test
    void rejectsBlankTextMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMessageType.TEXT,
                        "   ",
                        null
                )
        );
    }

    @Test
    void nonTextMessageMayHaveNoBody() {
        ConnectMessage message =
                new ConnectMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMessageType.FILE,
                        null,
                        null
                );

        assertNull(message.getBody());
    }

    @Test
    void preservesReplyReference() {
        UUID replyTo = UUID.randomUUID();

        ConnectMessage message =
                new ConnectMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMessageType.TEXT,
                        "Reply",
                        replyTo
                );

        assertEquals(
                replyTo,
                message.getReplyToMessageId()
        );
    }

    @Test
    void editsTextMessage() {
        ConnectMessage message = textMessage();

        message.edit(" Updated text ");

        assertEquals(
                "Updated text",
                message.getBody()
        );

        assertNotNull(message.getEditedAt());
    }

    @Test
    void rejectsBlankEditedText() {
        ConnectMessage message = textMessage();

        assertThrows(
                IllegalArgumentException.class,
                () -> message.edit("   ")
        );
    }

    @Test
    void deletedMessageCannotBeEdited() {
        ConnectMessage message = textMessage();

        message.delete();

        assertNotNull(message.getDeletedAt());

        assertThrows(
                IllegalStateException.class,
                () -> message.edit("Changed")
        );
    }

    @Test
    void rejectsMissingRequiredIdentifiers() {
        UUID id = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessage(
                        null,
                        id,
                        id,
                        ConnectMessageType.TEXT,
                        "Message",
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessage(
                        id,
                        null,
                        id,
                        ConnectMessageType.TEXT,
                        "Message",
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessage(
                        id,
                        id,
                        null,
                        ConnectMessageType.TEXT,
                        "Message",
                        null
                )
        );
    }

    @Test
    void rejectsMissingMessageType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Message",
                        null
                )
        );
    }

    private static ConnectMessage textMessage() {
        return new ConnectMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ConnectMessageType.TEXT,
                "Original",
                null
        );
    }
}
