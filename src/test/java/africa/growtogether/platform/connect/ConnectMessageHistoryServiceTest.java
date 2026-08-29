package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway.AttachmentReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectMessageHistoryServiceTest {

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
    void historyContainsExactEdsAttachmentMetadata() {

        stubOrdinarySpaceAccess();

        UUID messageId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        ConnectMessage message =
                message(
                        messageId
                );

        when(
                messages.findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        tenantId,
                        spaceId
                )
        ).thenReturn(
                List.of(
                        message
                )
        );

        ConnectMessageAttachment link =
                new ConnectMessageAttachment(
                        tenantId,
                        messageId,
                        documentId,
                        2
                );

        when(
                attachments.findAllByTenantIdAndMessageIdIn(
                        tenantId,
                        List.of(
                                messageId
                        )
                )
        ).thenReturn(
                List.of(
                        link
                )
        );

        when(
                edsAttachments.requireReadableVersion(
                        documentId,
                        2
                )
        ).thenReturn(
                new AttachmentReference(
                        documentId,
                        2,
                        "application/pdf",
                        24_000
                )
        );

        List<ConnectMessageHistoryItem> result =
                service.listMessageHistory(
                        spaceId
                );

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                message,
                result.getFirst().message()
        );

        assertEquals(
                1,
                result.getFirst()
                        .attachments()
                        .size()
        );

        var metadata =
                result.getFirst()
                        .attachments()
                        .getFirst();

        assertEquals(
                documentId,
                metadata.documentId()
        );

        assertEquals(
                2,
                metadata.documentVersion()
        );

        assertEquals(
                "application/pdf",
                metadata.mimeType()
        );

        assertEquals(
                24_000,
                metadata.sizeBytes()
        );

        verify(
                edsAttachments
        ).requireReadableVersion(
                documentId,
                2
        );
    }

    @Test
    void ordinaryTextMessageHasEmptyAttachmentList() {

        stubOrdinarySpaceAccess();

        UUID messageId =
                UUID.randomUUID();

        ConnectMessage message =
                message(
                        messageId
                );

        when(
                messages.findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        tenantId,
                        spaceId
                )
        ).thenReturn(
                List.of(
                        message
                )
        );

        when(
                attachments.findAllByTenantIdAndMessageIdIn(
                        tenantId,
                        List.of(
                                messageId
                        )
                )
        ).thenReturn(
                List.of()
        );

        List<ConnectMessageHistoryItem> result =
                service.listMessageHistory(
                        spaceId
                );

        assertEquals(
                1,
                result.size()
        );

        assertTrue(
                result.getFirst()
                        .attachments()
                        .isEmpty()
        );

        verifyNoInteractions(
                edsAttachments
        );
    }

    @Test
    void emptyConversationDoesNotQueryAttachmentStorage() {

        stubOrdinarySpaceAccess();

        when(
                messages.findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        tenantId,
                        spaceId
                )
        ).thenReturn(
                List.of()
        );

        List<ConnectMessageHistoryItem> result =
                service.listMessageHistory(
                        spaceId
                );

        assertTrue(
                result.isEmpty()
        );

        verifyNoInteractions(
                attachments
        );

        verifyNoInteractions(
                edsAttachments
        );
    }

    @Test
    void revokedEdsReadAccessPreventsAttachmentMetadataDisclosure() {

        stubOrdinarySpaceAccess();

        UUID messageId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        ConnectMessage message =
                message(
                        messageId
                );

        when(
                messages.findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        tenantId,
                        spaceId
                )
        ).thenReturn(
                List.of(
                        message
                )
        );

        when(
                attachments.findAllByTenantIdAndMessageIdIn(
                        tenantId,
                        List.of(
                                messageId
                        )
                )
        ).thenReturn(
                List.of(
                        new ConnectMessageAttachment(
                                tenantId,
                                messageId,
                                documentId,
                                1
                        )
                )
        );

        when(
                edsAttachments.requireReadableVersion(
                        documentId,
                        1
                )
        ).thenThrow(
                new AccessDeniedException(
                        "Document access denied"
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.listMessageHistory(
                                spaceId
                        )
        );
    }

    @Test
    void attachmentsAreBatchLoadedForConversationMessages() {

        stubOrdinarySpaceAccess();

        UUID firstId =
                UUID.randomUUID();

        UUID secondId =
                UUID.randomUUID();

        ConnectMessage firstMessage =
                message(
                        firstId
                );

        ConnectMessage secondMessage =
                message(
                        secondId
                );

        when(
                messages.findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        tenantId,
                        spaceId
                )
        ).thenReturn(
                List.of(
                        firstMessage,
                        secondMessage
                )
        );

        when(
                attachments.findAllByTenantIdAndMessageIdIn(
                        tenantId,
                        List.of(
                                firstId,
                                secondId
                        )
                )
        ).thenReturn(
                List.of()
        );

        service.listMessageHistory(
                spaceId
        );

        verify(
                attachments,
                times(1)
        ).findAllByTenantIdAndMessageIdIn(
                tenantId,
                List.of(
                        firstId,
                        secondId
                )
        );
    }

    private void stubOrdinarySpaceAccess() {

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        ConnectSpaceMember membership =
                mock(
                        ConnectSpaceMember.class
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

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
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

    private ConnectMessage message(
            UUID messageId
    ) {

        ConnectMessage message =
                mock(
                        ConnectMessage.class
                );

        when(
                message.getId()
        ).thenReturn(
                messageId
        );

        return message;
    }
}
