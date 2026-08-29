package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectMessageLifecycleServiceTest {

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
    private ConnectParentRelationshipAuthorizationService parentAuthorization;

    @Mock
    private ConnectTeacherAssignmentAuthorizationService teacherAssignmentAuthorization;

    private ConnectService service;

    private UUID tenantId;
    private UUID currentUserId;
    private UUID spaceId;
    private UUID messageId;

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

        tenantId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        spaceId = UUID.randomUUID();
        messageId = UUID.randomUUID();

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
    void originalSenderCanEditOwnTextMessage() {

        ConnectMessage message =
                textMessage(
                        currentUserId,
                        spaceId,
                        "Original"
                );

        stubOrdinarySpaceAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(message)
        );

        when(
                messages.save(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage result =
                service.editTextMessage(
                        spaceId,
                        messageId,
                        "Edited"
                );

        assertEquals(
                "Edited",
                result.getBody()
        );

        assertNotNull(
                result.getEditedAt()
        );

        verify(
                messages
        ).save(
                message
        );
    }

    @Test
    void differentUserCannotEditMessage() {

        UUID originalSender =
                UUID.randomUUID();

        ConnectMessage message =
                textMessage(
                        originalSender,
                        spaceId,
                        "Original"
                );

        stubOrdinarySpaceAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(message)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.editTextMessage(
                        spaceId,
                        messageId,
                        "Hostile edit"
                )
        );

        assertEquals(
                "Original",
                message.getBody()
        );

        verify(
                messages,
                never()
        ).save(
                any()
        );
    }

    @Test
    void senderCanDeleteOwnMessage() {

        ConnectMessage message =
                textMessage(
                        currentUserId,
                        spaceId,
                        "Delete me"
                );

        stubOrdinarySpaceAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(message)
        );

        when(
                messages.save(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage result =
                service.deleteMessage(
                        spaceId,
                        messageId
                );

        assertNotNull(
                result.getDeletedAt()
        );

        verify(
                messages
        ).save(
                message
        );
    }

    @Test
    void moderatorCanDeleteAnotherUsersMessage() {

        UUID originalSender =
                UUID.randomUUID();

        ConnectMessage message =
                textMessage(
                        originalSender,
                        spaceId,
                        "Moderated"
                );

        stubOrdinarySpaceAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(message)
        );

        when(
                identity.hasPermission(
                        ConnectPermissions.MODERATE
                )
        ).thenReturn(
                true
        );

        when(
                messages.save(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage result =
                service.deleteMessage(
                        spaceId,
                        messageId
                );

        assertNotNull(
                result.getDeletedAt()
        );

        verify(
                identity
        ).hasPermission(
                ConnectPermissions.MODERATE
        );
    }

    @Test
    void ordinaryMemberCannotDeleteAnotherUsersMessage() {

        UUID originalSender =
                UUID.randomUUID();

        ConnectMessage message =
                textMessage(
                        originalSender,
                        spaceId,
                        "Protected"
                );

        stubOrdinarySpaceAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(message)
        );

        when(
                identity.hasPermission(
                        ConnectPermissions.MODERATE
                )
        ).thenReturn(
                false
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.deleteMessage(
                        spaceId,
                        messageId
                )
        );

        assertNull(
                message.getDeletedAt()
        );

        verify(
                messages,
                never()
        ).save(
                any()
        );
    }

    @Test
    void messageFromAnotherSpaceCannotBeEdited() {

        UUID otherSpaceId =
                UUID.randomUUID();

        ConnectMessage message =
                textMessage(
                        currentUserId,
                        otherSpaceId,
                        "Wrong space"
                );

        stubOrdinarySpaceAccess();

        when(
                messages.findByIdAndTenantId(
                        messageId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(message)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.editTextMessage(
                        spaceId,
                        messageId,
                        "Should fail"
                )
        );

        assertEquals(
                "Wrong space",
                message.getBody()
        );

        verify(
                messages,
                never()
        ).save(
                any()
        );
    }

    @Test
    void revokedParentRelationshipBlocksLifecycleAccess() {

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
                Optional.of(space)
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
                Optional.of(membership)
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
                () -> service.editTextMessage(
                        spaceId,
                        messageId,
                        "Should never reach message"
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
                messages,
                never()
        ).save(
                any()
        );
    }

    private void stubOrdinarySpaceAccess() {

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
                Optional.of(space)
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
                Optional.of(membership)
        );
    }

    private ConnectMessage textMessage(
            UUID senderUserId,
            UUID messageSpaceId,
            String body
    ) {

        return new ConnectMessage(
                tenantId,
                messageSpaceId,
                senderUserId,
                ConnectMessageType.TEXT,
                body,
                null
        );
    }
}
