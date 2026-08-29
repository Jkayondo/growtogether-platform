package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectMessageReceiptServiceTest {

    @Mock
    private ConnectSpaceRepository spaces;

    @Mock
    private ConnectSpaceMemberRepository members;

    @Mock
    private ConnectMessageRepository messages;

    @Mock
    private ConnectMessageAttachmentRepository attachments;

    @Mock
    private EdsDocumentAttachmentGateway edsAttachments;

    @Mock
    private ConnectMessageReceiptRepository receipts;

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private ConnectParentRelationshipAuthorizationService parentAuthorization;

    @Mock
    private ConnectTeacherAssignmentAuthorizationService teacherAssignmentAuthorization;

    private ConnectService connectService;

    private ConnectMessageReceiptService service;

    private UUID tenantId;
    private UUID currentUserId;
    private UUID spaceId;
    private UUID messageId;

    @BeforeEach
    void setUp() {

        connectService =
                new ConnectService(
                        spaces,
                        members,
                        messages,
                        attachments,
                        edsAttachments,
                        identity,
                        parentAuthorization,
                        teacherAssignmentAuthorization
                );

        service =
                new ConnectMessageReceiptService(
                        connectService,
                        messages,
                        receipts,
                        identity
                );

        tenantId =
                UUID.randomUUID();

        currentUserId =
                UUID.randomUUID();

        spaceId =
                UUID.randomUUID();

        messageId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                currentUserId
        );
    }

    @Test
    void recipientCanMarkMessageDelivered() {

        UUID senderUserId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        ConnectMessage message =
                stubMessage(
                        spaceId,
                        senderUserId
                );

        when(
                receipts
                        .findByTenantIdAndMessageIdAndUserId(
                                tenantId,
                                messageId,
                                currentUserId
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                receipts.save(
                        any(ConnectMessageReceipt.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessageReceipt result =
                service.markDelivered(
                        spaceId,
                        messageId
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                messageId,
                result.getMessageId()
        );

        assertEquals(
                currentUserId,
                result.getUserId()
        );

        assertNotNull(
                result.getDeliveredAt()
        );

        assertNull(
                result.getReadAt()
        );

        verify(
                messages
        ).findByIdAndTenantId(
                messageId,
                tenantId
        );

        verify(
                receipts
        ).save(
                result
        );
    }

    @Test
    void recipientCanMarkMessageReadAndReadImpliesDelivered() {

        UUID senderUserId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubMessage(
                spaceId,
                senderUserId
        );

        when(
                receipts
                        .findByTenantIdAndMessageIdAndUserId(
                                tenantId,
                                messageId,
                                currentUserId
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                receipts.save(
                        any(ConnectMessageReceipt.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessageReceipt result =
                service.markRead(
                        spaceId,
                        messageId
                );

        assertNotNull(
                result.getDeliveredAt()
        );

        assertNotNull(
                result.getReadAt()
        );

        assertFalse(
                result.getReadAt()
                        .isBefore(
                                result.getDeliveredAt()
                        )
        );
    }

    @Test
    void existingReceiptIsReusedIdempotently() {

        UUID senderUserId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubMessage(
                spaceId,
                senderUserId
        );

        ConnectMessageReceipt existing =
                new ConnectMessageReceipt(
                        tenantId,
                        messageId,
                        currentUserId
                );

        existing.markDelivered();

        Instant firstDeliveredAt =
                existing.getDeliveredAt();

        when(
                receipts
                        .findByTenantIdAndMessageIdAndUserId(
                                tenantId,
                                messageId,
                                currentUserId
                        )
        ).thenReturn(
                Optional.of(
                        existing
                )
        );

        when(
                receipts.save(
                        existing
                )
        ).thenReturn(
                existing
        );

        ConnectMessageReceipt result =
                service.markDelivered(
                        spaceId,
                        messageId
                );

        assertSame(
                existing,
                result
        );

        assertEquals(
                firstDeliveredAt,
                result.getDeliveredAt()
        );

        verify(
                receipts
        ).save(
                existing
        );
    }

    @Test
    void senderCannotCreateReceiptForOwnMessage() {

        stubOrdinaryAccess();

        stubMessage(
                spaceId,
                currentUserId
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markRead(
                        spaceId,
                        messageId
                )
        );

        verify(
                receipts,
                never()
        ).findByTenantIdAndMessageIdAndUserId(
                any(),
                any(),
                any()
        );

        verify(
                receipts,
                never()
        ).save(
                any()
        );
    }

    @Test
    void messageFromAnotherSpaceCannotReceiveReceipt() {

        UUID senderUserId =
                UUID.randomUUID();

        UUID otherSpaceId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubMessage(
                otherSpaceId,
                senderUserId
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markDelivered(
                        spaceId,
                        messageId
                )
        );

        verify(
                receipts,
                never()
        ).save(
                any()
        );
    }

    @Test
    void tenantScopedMessageLookupIsRequired() {

        stubOrdinaryAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markDelivered(
                        spaceId,
                        messageId
                )
        );

        verify(
                messages
        ).findByIdAndTenantId(
                messageId,
                tenantId
        );

        verify(
                receipts,
                never()
        ).save(
                any()
        );
    }

    @Test
    void staleParentRelationshipBlocksReceiptDespiteActiveMembership() {

        UUID studentId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STUDENT
        );

        when(
                space.getContextReference()
        ).thenReturn(
                studentId.toString()
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        currentUserId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(
                        membership
                )
        );

        doThrow(
                new AccessDeniedException(
                        "Parent relationship is no longer authorised"
                )
        ).when(
                parentAuthorization
        ).requireUserCanCommunicateWithStudent(
                currentUserId,
                studentId
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.markRead(
                        spaceId,
                        messageId
                )
        );

        verify(
                messages,
                never()
        ).findByIdAndTenantId(
                any(),
                any()
        );

        verify(
                receipts,
                never()
        ).save(
                any()
        );
    }

    @Test
    void staleTeacherAssignmentBlocksReceiptDespiteActiveMembership() {

        UUID streamId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STREAM
        );

        when(
                space.getContextReference()
        ).thenReturn(
                streamId.toString()
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        currentUserId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(
                        membership
                )
        );

        doThrow(
                new AccessDeniedException(
                        "Teacher assignment is no longer authorised"
                )
        ).when(
                teacherAssignmentAuthorization
        ).requireTeacherCanAccessStream(
                currentUserId,
                streamId
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.markDelivered(
                        spaceId,
                        messageId
                )
        );

        verify(
                messages,
                never()
        ).findByIdAndTenantId(
                any(),
                any()
        );

        verify(
                receipts,
                never()
        ).save(
                any()
        );
    }

    @Test
    void messageSenderCanRetrieveReceiptStatus() {

        stubOrdinaryAccess();

        stubRetrievalMessage(
                spaceId,
                currentUserId
        );

        UUID recipientUserId =
                UUID.randomUUID();

        ConnectMessageReceipt receipt =
                new ConnectMessageReceipt(
                        tenantId,
                        messageId,
                        recipientUserId
                );

        receipt.markRead();

        when(
                receipts.findAllByTenantIdAndMessageId(
                        tenantId,
                        messageId
                )
        ).thenReturn(
                List.of(
                        receipt
                )
        );

        List<ConnectMessageReceipt> result =
                service.getReceiptStatus(
                        spaceId,
                        messageId
                );

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                receipt,
                result.get(0)
        );

        verify(
                receipts
        ).findAllByTenantIdAndMessageId(
                tenantId,
                messageId
        );
    }


    @Test
    void connectManagerCanRetrieveReceiptStatus() {

        UUID senderUserId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubRetrievalMessage(
                spaceId,
                senderUserId
        );

        doReturn(
                true
        ).when(
                identity
        ).hasPermission(
                ConnectPermissions.MANAGE
        );

        doReturn(
                false
        ).when(
                identity
        ).hasPermission(
                ConnectPermissions.MODERATE
        );

        ConnectMessageReceipt receipt =
                new ConnectMessageReceipt(
                        tenantId,
                        messageId,
                        UUID.randomUUID()
                );

        receipt.markDelivered();

        when(
                receipts.findAllByTenantIdAndMessageId(
                        tenantId,
                        messageId
                )
        ).thenReturn(
                List.of(
                        receipt
                )
        );

        List<ConnectMessageReceipt> result =
                service.getReceiptStatus(
                        spaceId,
                        messageId
                );

        assertEquals(
                1,
                result.size()
        );

        verify(
                receipts
        ).findAllByTenantIdAndMessageId(
                tenantId,
                messageId
        );
    }


    @Test
    void connectModeratorCanRetrieveReceiptStatus() {

        UUID senderUserId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubRetrievalMessage(
                spaceId,
                senderUserId
        );

        doReturn(
                false
        ).when(
                identity
        ).hasPermission(
                ConnectPermissions.MANAGE
        );

        doReturn(
                true
        ).when(
                identity
        ).hasPermission(
                ConnectPermissions.MODERATE
        );

        when(
                receipts.findAllByTenantIdAndMessageId(
                        tenantId,
                        messageId
                )
        ).thenReturn(
                List.of()
        );

        List<ConnectMessageReceipt> result =
                service.getReceiptStatus(
                        spaceId,
                        messageId
                );

        assertTrue(
                result.isEmpty()
        );

        verify(
                receipts
        ).findAllByTenantIdAndMessageId(
                tenantId,
                messageId
        );
    }


    @Test
    void ordinaryParticipantCannotRetrieveOtherUsersReceiptStatus() {

        UUID senderUserId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubRetrievalMessage(
                spaceId,
                senderUserId
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.getReceiptStatus(
                        spaceId,
                        messageId
                )
        );

        verify(
                receipts,
                never()
        ).findAllByTenantIdAndMessageId(
                any(),
                any()
        );
    }


    @Test
    void messageFromAnotherSpaceCannotExposeReceiptStatus() {

        UUID senderUserId =
                UUID.randomUUID();

        UUID otherSpaceId =
                UUID.randomUUID();

        stubOrdinaryAccess();

        stubRetrievalMessage(
                otherSpaceId,
                senderUserId
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getReceiptStatus(
                        spaceId,
                        messageId
                )
        );

        verify(
                receipts,
                never()
        ).findAllByTenantIdAndMessageId(
                any(),
                any()
        );

        verify(
                identity,
                never()
        ).hasPermission(
                any()
        );
    }


    private ConnectMessage stubRetrievalMessage(
            UUID messageSpaceId,
            UUID senderUserId
    ) {

        ConnectMessage message =
                mock(
                        ConnectMessage.class
                );

        when(
                message.getSpaceId()
        ).thenReturn(
                messageSpaceId
        );

        if (
                spaceId.equals(
                        messageSpaceId
                )
        ) {
            when(
                    message.getSenderUserId()
            ).thenReturn(
                    senderUserId
            );
        }

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        message
                )
        );

        return message;
    }


    private void stubOrdinaryAccess() {

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        currentUserId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(
                        membership
                )
        );
    }

    private ConnectMessage stubMessage(
            UUID messageSpaceId,
            UUID senderUserId
    ) {

        ConnectMessage message =
                mock(
                        ConnectMessage.class
                );

        /*
         * getSpaceId() is always evaluated first.
         */
        when(
                message.getSpaceId()
        ).thenReturn(
                messageSpaceId
        );

        /*
         * The sender is evaluated only when the message
         * actually belongs to the requested space.
         */
        if (
                spaceId.equals(
                        messageSpaceId
                )
        ) {

            when(
                    message.getSenderUserId()
            ).thenReturn(
                    senderUserId
            );

            /*
             * getId() is needed only after the sender check
             * succeeds and receipt lookup/creation begins.
             */
            if (
                    !currentUserId.equals(
                            senderUserId
                    )
            ) {

                when(
                        message.getId()
                ).thenReturn(
                        messageId
                );
            }
        }

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        message
                )
        );

        return message;
    }
}
