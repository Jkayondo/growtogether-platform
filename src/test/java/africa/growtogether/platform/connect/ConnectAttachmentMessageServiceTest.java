package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway.AttachmentReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectAttachmentMessageServiceTest {

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
    private EnterpriseIdentityContext identity;

    @Mock
    private ConnectParentRelationshipAuthorizationService
            parentAuthorization;

    @Mock
    private ConnectTeacherAssignmentAuthorizationService
            teacherAssignmentAuthorization;

    private ConnectService service;

    private UUID tenantId;
    private UUID currentUserId;
    private UUID spaceId;

    @BeforeEach
    void setUp() {

        service =
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

        tenantId =
                UUID.randomUUID();

        currentUserId =
                UUID.randomUUID();

        spaceId =
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
    void imageAttachmentCreatesImageMessageAndImmutableEdsReference() {

        stubOrdinarySpaceAccess();

        UUID documentId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        when(
                edsAttachments.requireAttachable(
                        documentId
                )
        ).thenReturn(
                new AttachmentReference(
                        documentId,
                        3,
                        "image/png",
                        12_345
                )
        );

        ConnectMessage persisted =
                mock(ConnectMessage.class);

        when(
                persisted.getId()
        ).thenReturn(
                messageId
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenReturn(
                persisted
        );

        when(
                attachments.saveAndFlush(
                        any(ConnectMessageAttachment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage result =
                service.sendAttachmentMessage(
                        spaceId,
                        documentId,
                        "School photograph",
                        null
                );

        assertSame(
                persisted,
                result
        );

        ArgumentCaptor<ConnectMessage>
                messageCaptor =
                ArgumentCaptor.forClass(
                        ConnectMessage.class
                );

        verify(
                messages
        ).saveAndFlush(
                messageCaptor.capture()
        );

        ConnectMessage created =
                messageCaptor.getValue();

        assertEquals(
                ConnectMessageType.IMAGE,
                created.getMessageType()
        );

        assertEquals(
                spaceId,
                created.getSpaceId()
        );

        assertEquals(
                currentUserId,
                created.getSenderUserId()
        );

        assertEquals(
                "School photograph",
                created.getBody()
        );

        ArgumentCaptor<ConnectMessageAttachment>
                attachmentCaptor =
                ArgumentCaptor.forClass(
                        ConnectMessageAttachment.class
                );

        verify(
                attachments
        ).saveAndFlush(
                attachmentCaptor.capture()
        );

        ConnectMessageAttachment attachment =
                attachmentCaptor.getValue();

        assertEquals(
                tenantId,
                attachment.getTenantId()
        );

        assertEquals(
                messageId,
                attachment.getMessageId()
        );

        assertEquals(
                documentId,
                attachment.getDocumentId()
        );

        assertEquals(
                3,
                attachment.getDocumentVersion()
        );
    }

    @Test
    void nonMediaMimeTypeCreatesFileMessage() {

        stubOrdinarySpaceAccess();

        UUID documentId =
                UUID.randomUUID();

        ConnectMessage persisted =
                mock(ConnectMessage.class);

        when(
                persisted.getId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                edsAttachments.requireAttachable(
                        documentId
                )
        ).thenReturn(
                new AttachmentReference(
                        documentId,
                        2,
                        "application/pdf",
                        50_000
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenReturn(
                persisted
        );

        when(
                attachments.saveAndFlush(
                        any(ConnectMessageAttachment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        service.sendAttachmentMessage(
                spaceId,
                documentId,
                null,
                null
        );

        ArgumentCaptor<ConnectMessage>
                captor =
                ArgumentCaptor.forClass(
                        ConnectMessage.class
                );

        verify(
                messages
        ).saveAndFlush(
                captor.capture()
        );

        assertEquals(
                ConnectMessageType.FILE,
                captor.getValue().getMessageType()
        );
    }

    @Test
    void edsDenialPreventsConnectMessagePersistence() {

        stubOrdinarySpaceAccess();

        UUID documentId =
                UUID.randomUUID();

        when(
                edsAttachments.requireAttachable(
                        documentId
                )
        ).thenThrow(
                new AccessDeniedException(
                        "Document access denied"
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.sendAttachmentMessage(
                                spaceId,
                                documentId,
                                null,
                                null
                        )
        );

        verify(
                messages,
                never()
        ).saveAndFlush(
                any()
        );

        verifyNoInteractions(
                attachments
        );
    }

    @Test
    void nonMemberCannotReachEdsAttachmentGateway() {

        ConnectSpace space =
                mock(ConnectSpace.class);

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

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.sendAttachmentMessage(
                                spaceId,
                                UUID.randomUUID(),
                                null,
                                null
                        )
        );

        verifyNoInteractions(
                edsAttachments
        );

        verify(
                messages,
                never()
        ).saveAndFlush(
                any()
        );

        verifyNoInteractions(
                attachments
        );
    }

    @Test
    void attachmentCannotReplyToMessageInAnotherSpace() {

        stubOrdinarySpaceAccess();

        UUID replyId =
                UUID.randomUUID();

        ConnectMessage reply =
                mock(ConnectMessage.class);

        when(
                reply.getSpaceId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                messages.findByIdAndTenantId(
                        replyId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        reply
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.sendAttachmentMessage(
                                spaceId,
                                UUID.randomUUID(),
                                null,
                                replyId
                        )
        );

        verifyNoInteractions(
                edsAttachments
        );

        verify(
                messages,
                never()
        ).saveAndFlush(
                any()
        );

        verifyNoInteractions(
                attachments
        );
    }

    @Test
    void attachmentIsNotSavedWhenMessageHasNoPersistentId() {

        stubOrdinarySpaceAccess();

        UUID documentId =
                UUID.randomUUID();

        when(
                edsAttachments.requireAttachable(
                        documentId
                )
        ).thenReturn(
                new AttachmentReference(
                        documentId,
                        1,
                        "audio/mpeg",
                        5_000
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.sendAttachmentMessage(
                                spaceId,
                                documentId,
                                null,
                                null
                        )
        );

        verifyNoInteractions(
                attachments
        );
    }

    private void stubOrdinarySpaceAccess() {

        ConnectSpace space =
                mock(ConnectSpace.class);

        ConnectSpaceMember membership =
                mock(ConnectSpaceMember.class);

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
}
