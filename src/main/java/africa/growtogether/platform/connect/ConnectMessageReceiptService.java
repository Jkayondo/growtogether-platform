package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConnectMessageReceiptService {

    private final ConnectService connectService;

    private final ConnectMessageRepository messages;

    private final ConnectMessageReceiptRepository receipts;

    private final EnterpriseIdentityContext identity;

    public ConnectMessageReceiptService(
            ConnectService connectService,
            ConnectMessageRepository messages,
            ConnectMessageReceiptRepository receipts,
            EnterpriseIdentityContext identity
    ) {
        this.connectService = connectService;
        this.messages = messages;
        this.receipts = receipts;
        this.identity = identity;
    }

    @Transactional
    public ConnectMessageReceipt markDelivered(
            UUID spaceId,
            UUID messageId
    ) {

        ReceiptContext context =
                requireReceiptContext(
                        spaceId,
                        messageId
                );

        ConnectMessageReceipt receipt =
                findOrCreate(
                        context
                );

        receipt.markDelivered();

        return receipts.save(
                receipt
        );
    }

    @Transactional
    public ConnectMessageReceipt markRead(
            UUID spaceId,
            UUID messageId
    ) {

        ReceiptContext context =
                requireReceiptContext(
                        spaceId,
                        messageId
                );

        ConnectMessageReceipt receipt =
                findOrCreate(
                        context
                );

        receipt.markRead();

        return receipts.save(
                receipt
        );
    }

    @Transactional(readOnly = true)
    public List<ConnectMessageReceipt> getReceiptStatus(
            UUID spaceId,
            UUID messageId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        /*
         * Reuse the authoritative Connect access boundary.
         *
         * This preserves:
         * - tenant isolation
         * - ACTIVE membership
         * - protected parent relationship revalidation
         * - protected teacher assignment revalidation
         * - protected owner management authority
         */
        connectService.getSpace(
                spaceId
        );

        ConnectMessage message =
                messages
                        .findByIdAndTenantId(
                                messageId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "GT Connect message was not found"
                                )
                        );

        if (
                !spaceId.equals(
                        message.getSpaceId()
                )
        ) {
            throw new IllegalArgumentException(
                    "GT Connect message belongs to another space"
            );
        }

        boolean sender =
                currentUserId.equals(
                        message.getSenderUserId()
                );

        boolean manager =
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                );

        boolean moderator =
                identity.hasPermission(
                        ConnectPermissions.MODERATE
                );

        if (
                !sender
                        && !manager
                        && !moderator
        ) {
            throw new AccessDeniedException(
                    "Only the message sender or an authorised GT Connect manager/moderator may view receipt status"
            );
        }

        return receipts
                .findAllByTenantIdAndMessageId(
                        tenantId,
                        messageId
                );
    }


    private ReceiptContext requireReceiptContext(
            UUID spaceId,
            UUID messageId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        /*
         * This is deliberately reused rather than duplicated.
         *
         * ConnectService.getSpace() verifies:
         * - tenant scope
         * - ACTIVE Connect membership
         * - protected parent relationship
         * - protected teacher assignment
         * - protected owner management authority
         */
        connectService.getSpace(
                spaceId
        );

        ConnectMessage message =
                messages
                        .findByIdAndTenantId(
                                messageId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "GT Connect message was not found"
                                )
                        );

        if (
                !spaceId.equals(
                        message.getSpaceId()
                )
        ) {
            throw new IllegalArgumentException(
                    "GT Connect message belongs to another space"
            );
        }

        if (
                currentUserId.equals(
                        message.getSenderUserId()
                )
        ) {
            throw new IllegalArgumentException(
                    "GT Connect sender does not require a receipt for their own message"
            );
        }

        return new ReceiptContext(
                tenantId,
                currentUserId,
                message
        );
    }

    private ConnectMessageReceipt findOrCreate(
            ReceiptContext context
    ) {

        return receipts
                .findByTenantIdAndMessageIdAndUserId(
                        context.tenantId(),
                        context.message().getId(),
                        context.userId()
                )
                .orElseGet(
                        () -> new ConnectMessageReceipt(
                                context.tenantId(),
                                context.message().getId(),
                                context.userId()
                        )
                );
    }

    private record ReceiptContext(
            UUID tenantId,
            UUID userId,
            ConnectMessage message
    ) {
    }
}
