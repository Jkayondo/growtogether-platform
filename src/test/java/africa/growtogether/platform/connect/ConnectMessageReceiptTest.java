package africa.growtogether.platform.connect;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectMessageReceiptTest {

    @Test
    void deliveredIsIdempotent() {

        ConnectMessageReceipt receipt =
                receipt();

        receipt.markDelivered();

        Instant first =
                receipt.getDeliveredAt();

        assertNotNull(first);
        assertNull(
                receipt.getReadAt()
        );

        receipt.markDelivered();

        assertEquals(
                first,
                receipt.getDeliveredAt()
        );
    }

    @Test
    void readAutomaticallyImpliesDelivered() {

        ConnectMessageReceipt receipt =
                receipt();

        receipt.markRead();

        assertNotNull(
                receipt.getDeliveredAt()
        );

        assertNotNull(
                receipt.getReadAt()
        );

        assertFalse(
                receipt.getReadAt()
                        .isBefore(
                                receipt.getDeliveredAt()
                        )
        );
    }

    @Test
    void readIsIdempotent() {

        ConnectMessageReceipt receipt =
                receipt();

        receipt.markRead();

        Instant delivered =
                receipt.getDeliveredAt();

        Instant read =
                receipt.getReadAt();

        receipt.markRead();

        assertEquals(
                delivered,
                receipt.getDeliveredAt()
        );

        assertEquals(
                read,
                receipt.getReadAt()
        );
    }

    @Test
    void requiredIdentityFieldsCannotBeNull() {

        UUID tenantId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessageReceipt(
                        null,
                        messageId,
                        userId
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessageReceipt(
                        tenantId,
                        null,
                        userId
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectMessageReceipt(
                        tenantId,
                        messageId,
                        null
                )
        );
    }

    private ConnectMessageReceipt receipt() {

        return new ConnectMessageReceipt(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }
}
