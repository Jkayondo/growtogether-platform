package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectServiceTest {

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
    }

    @Test
    void sendMessageUsesAuthenticatedUserAsSender() {

        stubTenantAndUser();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(space)
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
                        mock(ConnectSpaceMember.class)
                )
        );

        when(
                messages.save(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage created =
                service.sendTextMessage(
                        spaceId,
                        "Hello GT Connect",
                        null
                );

        assertEquals(
                currentUserId,
                created.getSenderUserId()
        );

        assertEquals(
                tenantId,
                created.getTenantId()
        );

        assertEquals(
                "Hello GT Connect",
                created.getBody()
        );
    }

    @Test
    void nonMemberCannotSendMessage() {

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
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
                () -> service.sendTextMessage(
                        spaceId,
                        "Not allowed",
                        null
                )
        );

        verify(
                messages,
                never()
        ).save(
                any()
        );
    }

    @Test
    void cannotReplyToMessageFromAnotherSpace() {

        stubTenantAndUser();

        UUID replyId =
                UUID.randomUUID();

        ConnectMessage replied =
                new ConnectMessage(
                        tenantId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ConnectMessageType.TEXT,
                        "Original",
                        null
                );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
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
                        mock(ConnectSpaceMember.class)
                )
        );

        when(
                messages.findByIdAndTenantId(
                        replyId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(replied)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendTextMessage(
                        spaceId,
                        "Reply",
                        replyId
                )
        );
    }

    @Test
    void nonMemberCannotReadConversation() {

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
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
                () -> service.listMessages(
                        spaceId
                )
        );

        verify(
                messages,
                never()
        )
                .findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        any(),
                        any()
                );
    }

    @Test
    void activeMemberCanReadConversation() {

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
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
                        mock(ConnectSpaceMember.class)
                )
        );

        when(
                messages
                        .findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                                tenantId,
                                spaceId
                        )
        ).thenReturn(
                List.of()
        );

        assertTrue(
                service.listMessages(
                        spaceId
                ).isEmpty()
        );
    }

    @Test
    void managementPermissionIsRequiredToAddMember() {

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                false
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.addMember(
                        spaceId,
                        UUID.randomUUID(),
                        ConnectMemberRole.MEMBER
                )
        );

        verifyNoInteractions(
                spaces,
                members
        );
    }

    @Test
    void managerCanAddMember() {

        UUID newUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenant();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
                )
        );

        when(
                members
                        .existsByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                newUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                false
        );

        when(
                members.save(
                        any(ConnectSpaceMember.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectSpaceMember created =
                service.addMember(
                        spaceId,
                        newUserId,
                        ConnectMemberRole.MEMBER
                );

        assertEquals(
                tenantId,
                created.getTenantId()
        );

        assertEquals(
                spaceId,
                created.getSpaceId()
        );

        assertEquals(
                newUserId,
                created.getUserId()
        );
    }

    @Test
    void managerCanAddOrdinaryMemberToSchoolProfileSpace() {

        UUID newUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                "SCHOOL_PROFILE"
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
                members
                        .existsByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                newUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                false
        );

        when(
                members.save(
                        any(ConnectSpaceMember.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        ConnectSpaceMember created =
                service.addMember(
                        spaceId,
                        newUserId,
                        ConnectMemberRole.MEMBER
                );


        assertEquals(
                ConnectMemberRole.MEMBER,
                created.getMemberRole()
        );

        assertEquals(
                newUserId,
                created.getUserId()
        );
    }


    @Test
    void managerCannotManuallyAddAdminToSchoolProfileSpace() {

        UUID newUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                "SCHOOL_PROFILE"
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


        assertThrows(
                AccessDeniedException.class,
                () -> service.addMember(
                        spaceId,
                        newUserId,
                        ConnectMemberRole.ADMIN
                )
        );


        verify(
                members,
                never()
        ).save(
                any()
        );
    }


    @Test
    void managerCannotManuallyAddModeratorToSchoolProfileSpace() {

        UUID newUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                "SCHOOL_PROFILE"
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


        assertThrows(
                AccessDeniedException.class,
                () -> service.addMember(
                        spaceId,
                        newUserId,
                        ConnectMemberRole.MODERATOR
                )
        );


        verify(
                members,
                never()
        ).save(
                any()
        );
    }


    @Test
    void managerCannotManuallyAddOwnerToSchoolProfileSpace() {

        UUID newUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                "SCHOOL_PROFILE"
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


        assertThrows(
                AccessDeniedException.class,
                () -> service.addMember(
                        spaceId,
                        newUserId,
                        ConnectMemberRole.OWNER
                )
        );


        verify(
                members,
                never()
        ).save(
                any()
        );
    }


    @Test
    void activeMemberCanLeaveSpace() {

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
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
                Optional.of(membership)
        );

        when(
                members.save(
                        any(ConnectSpaceMember.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectSpaceMember result =
                service.leaveSpace(
                        spaceId
                );

        assertEquals(
                ConnectMembershipStatus.LEFT,
                result.getMembershipStatus()
        );

        assertNotNull(
                result.getLeftAt()
        );
    }

    @Test
    void ownerCannotLeaveSpaceWithoutOwnershipTransfer() {

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
                )
        );

        ConnectSpaceMember owner =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        currentUserId,
                        ConnectMemberRole.OWNER
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
                Optional.of(owner)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.leaveSpace(
                        spaceId
                )
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void managerCanRemoveActiveMember() {

        UUID targetUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
                )
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        targetUserId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                targetUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(membership)
        );

        when(
                members.save(
                        any(ConnectSpaceMember.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectSpaceMember result =
                service.removeMember(
                        spaceId,
                        targetUserId
                );

        assertEquals(
                ConnectMembershipStatus.REMOVED,
                result.getMembershipStatus()
        );

        assertNotNull(
                result.getLeftAt()
        );
    }

    @Test
    void managerCannotRemoveSpaceOwner() {

        UUID ownerUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenantAndUser();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(ConnectSpace.class)
                )
        );

        ConnectSpaceMember owner =
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        ownerUserId,
                        ConnectMemberRole.OWNER
                );

        when(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                ownerUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(owner)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.removeMember(
                        spaceId,
                        ownerUserId
                )
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void managerCannotUseRemoveMemberToRemoveSelf() {

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        stubTenantAndUser();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.removeMember(
                        spaceId,
                        currentUserId
                )
        );

        verifyNoInteractions(
                spaces,
                members
        );
    }

    @Test
    void genericMembershipCannotBypassStudentRelationshipAuthorization() {

        UUID parentUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STUDENT
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(space)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.addMember(
                        spaceId,
                        parentUserId,
                        ConnectMemberRole.MEMBER
                )
        );

        verifyNoInteractions(
                parentAuthorization
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void managerCanAddAuthorizedParentToStudentSpace() {

        UUID parentUserId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

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

        when(
                members
                        .existsByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                parentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(
                members.save(
                        any(ConnectSpaceMember.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectSpaceMember created =
                service.addParentMember(
                        spaceId,
                        parentUserId,
                        ConnectMemberRole.MEMBER
                );

        verify(
                parentAuthorization
        ).requireUserCanCommunicateWithStudent(
                parentUserId,
                studentId
        );

        assertEquals(
                parentUserId,
                created.getUserId()
        );

        assertEquals(
                spaceId,
                created.getSpaceId()
        );
    }

    @Test
    void unauthorizedParentIsNotAddedToStudentSpace() {

        UUID parentUserId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

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

        when(
                parentAuthorization
                        .requireUserCanCommunicateWithStudent(
                                parentUserId,
                                studentId
                        )
        ).thenThrow(
                new AccessDeniedException(
                        "Not authorised"
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.addParentMember(
                        spaceId,
                        parentUserId,
                        ConnectMemberRole.MEMBER
                )
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void studentSpaceRequiresUuidContextReference() {

        UUID parentUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STUDENT
        );

        when(
                space.getContextReference()
        ).thenReturn(
                "not-a-student-uuid"
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(space)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addParentMember(
                        spaceId,
                        parentUserId,
                        ConnectMemberRole.MEMBER
                )
        );

        verifyNoInteractions(
                parentAuthorization
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void genericMembershipCannotBypassStreamAssignmentAuthorization() {

        UUID teacherUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STREAM
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(space)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.addMember(
                        spaceId,
                        teacherUserId,
                        ConnectMemberRole.MEMBER
                )
        );

        verifyNoInteractions(
                teacherAssignmentAuthorization
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void managerCanAddAssignedTeacherToStreamSpace() {

        UUID teacherUserId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

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
                Optional.of(space)
        );

        when(
                members
                        .existsByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                teacherUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(
                members.save(
                        any(ConnectSpaceMember.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectSpaceMember created =
                service.addTeacherMember(
                        spaceId,
                        teacherUserId,
                        ConnectMemberRole.MEMBER
                );

        verify(
                teacherAssignmentAuthorization
        ).requireTeacherCanAccessStream(
                teacherUserId,
                streamId
        );

        assertEquals(
                teacherUserId,
                created.getUserId()
        );
    }

    @Test
    void unassignedTeacherIsNotAddedToStreamSpace() {

        UUID teacherUserId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

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
                Optional.of(space)
        );

        when(
                teacherAssignmentAuthorization
                        .requireTeacherCanAccessStream(
                                teacherUserId,
                                streamId
                        )
        ).thenThrow(
                new AccessDeniedException(
                        "Teacher is not assigned"
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.addTeacherMember(
                        spaceId,
                        teacherUserId,
                        ConnectMemberRole.MEMBER
                )
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }

    @Test
    void streamSpaceRequiresUuidContextReference() {

        UUID teacherUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        stubTenant();

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STREAM
        );

        when(
                space.getContextReference()
        ).thenReturn(
                "not-a-stream-uuid"
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(space)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addTeacherMember(
                        spaceId,
                        teacherUserId,
                        ConnectMemberRole.MEMBER
                )
        );

        verifyNoInteractions(
                teacherAssignmentAuthorization
        );

        verify(
                members,
                never()
        ).save(
                any()
        );
    }


    @Test
    void protectedParentCannotReceiveElevatedRole() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID parentUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.addParentMember(
                                protectedSpaceId,
                                parentUserId,
                                ConnectMemberRole.ADMIN
                        )
        );

        verifyNoInteractions(
                parentAuthorization
        );
    }

    @Test
    void protectedTeacherCannotReceiveElevatedRole() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID teacherUserId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(true);

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.addTeacherMember(
                                protectedSpaceId,
                                teacherUserId,
                                ConnectMemberRole.MODERATOR
                        )
        );

        verifyNoInteractions(
                teacherAssignmentAuthorization
        );
    }

    @Test
    void staleParentRelationshipCannotSendDespiteActiveMembership() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                userId
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

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
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        doThrow(
                new AccessDeniedException(
                        "Relationship no longer authorised"
                )
        ).when(
                parentAuthorization
        ).requireUserCanCommunicateWithStudent(
                userId,
                studentId
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.sendTextMessage(
                                protectedSpaceId,
                                "Hello",
                                null
                        )
        );

        verify(
                messages,
                never()
        ).save(
                any()
        );
    }

    @Test
    void validParentRelationshipIsRevalidatedBeforeSend() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                userId
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

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
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        when(
                messages.save(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage message =
                service.sendTextMessage(
                        protectedSpaceId,
                        "Hello",
                        null
                );

        assertNotNull(
                message
        );

        verify(
                parentAuthorization
        ).requireUserCanCommunicateWithStudent(
                userId,
                studentId
        );
    }

    @Test
    void staleTeacherAssignmentCannotReadMessagesDespiteActiveMembership() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                userId
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

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
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        doThrow(
                new AccessDeniedException(
                        "Teaching assignment no longer authorised"
                )
        ).when(
                teacherAssignmentAuthorization
        ).requireTeacherCanAccessStream(
                userId,
                streamId
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.listMessages(
                                protectedSpaceId
                        )
        );

        verify(
                messages,
                never()
        ).findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                tenant,
                protectedSpaceId
        );
    }

    @Test
    void validTeacherAssignmentIsRevalidatedBeforeSpaceRead() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                userId
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

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
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMemberRole.MEMBER
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        assertSame(
                space,
                service.getSpace(
                        protectedSpaceId
                )
        );

        verify(
                teacherAssignmentAuthorization
        ).requireTeacherCanAccessStream(
                userId,
                streamId
        );
    }

    @Test
    void protectedOwnerCanAccessWithCurrentManagePermission() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID ownerUserId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                ownerUserId
        );

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                true
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STUDENT
        );

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        ownerUserId,
                        ConnectMemberRole.OWNER
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        ownerUserId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        assertSame(
                space,
                service.getSpace(
                        protectedSpaceId
                )
        );

        verifyNoInteractions(
                parentAuthorization,
                teacherAssignmentAuthorization
        );
    }

    @Test
    void protectedOwnerWithoutManagePermissionIsDenied() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID ownerUserId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                ownerUserId
        );

        when(
                identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ).thenReturn(
                false
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STREAM
        );

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        ownerUserId,
                        ConnectMemberRole.OWNER
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        ownerUserId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.getSpace(
                                protectedSpaceId
                        )
        );

        verifyNoInteractions(
                parentAuthorization,
                teacherAssignmentAuthorization
        );
    }

    @Test
    void protectedAdminMembershipIsDenied() {

        UUID tenant =
                UUID.randomUUID();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenant
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                userId
        );

        ConnectSpace space =
                mock(ConnectSpace.class);

        when(
                space.getContextType()
        ).thenReturn(
                ConnectContextTypes.SCHOOL_STUDENT
        );

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenant
                )
        ).thenReturn(
                Optional.of(space)
        );

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMemberRole.ADMIN
                );

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenant,
                        protectedSpaceId,
                        userId,
                        ConnectMembershipStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(membership)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.getSpace(
                                protectedSpaceId
                        )
        );

        verifyNoInteractions(
                parentAuthorization,
                teacherAssignmentAuthorization
        );
    }

    @Test
    void listMySpacesReturnsCurrentUsersActiveConversation() {

        stubTenantAndUser();

        UUID conversationId =
                UUID.randomUUID();

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        conversationId,
                        currentUserId,
                        ConnectMemberRole.MEMBER
                );

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                members
                        .findAllByTenantIdAndUserIdAndMembershipStatus(
                                tenantId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        membership
                )
        );

        when(
                spaces.findByIdAndTenantId(
                        conversationId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        List<ConnectSpace> result =
                service.listMySpaces();

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                space,
                result.get(0)
        );

        verify(
                members
        ).findAllByTenantIdAndUserIdAndMembershipStatus(
                tenantId,
                currentUserId,
                ConnectMembershipStatus.ACTIVE
        );

        verify(
                spaces
        ).findByIdAndTenantId(
                conversationId,
                tenantId
        );
    }


    @Test
    void staleParentConversationIsOmittedFromMySpaces() {

        stubTenantAndUser();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        protectedSpaceId,
                        currentUserId,
                        ConnectMemberRole.MEMBER
                );

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
                members
                        .findAllByTenantIdAndUserIdAndMembershipStatus(
                                tenantId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        membership
                )
        );

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
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

        List<ConnectSpace> result =
                service.listMySpaces();

        assertTrue(
                result.isEmpty()
        );

        verify(
                parentAuthorization
        ).requireUserCanCommunicateWithStudent(
                currentUserId,
                studentId
        );
    }


    @Test
    void staleTeacherConversationIsOmittedFromMySpaces() {

        stubTenantAndUser();

        UUID protectedSpaceId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        ConnectSpaceMember membership =
                new ConnectSpaceMember(
                        tenantId,
                        protectedSpaceId,
                        currentUserId,
                        ConnectMemberRole.MEMBER
                );

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
                members
                        .findAllByTenantIdAndUserIdAndMembershipStatus(
                                tenantId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        membership
                )
        );

        when(
                spaces.findByIdAndTenantId(
                        protectedSpaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
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

        List<ConnectSpace> result =
                service.listMySpaces();

        assertTrue(
                result.isEmpty()
        );

        verify(
                teacherAssignmentAuthorization
        ).requireTeacherCanAccessStream(
                currentUserId,
                streamId
        );
    }


    @Test
    void listMySpacesUsesAuthenticatedTenantAndUserOnly() {

        stubTenantAndUser();

        when(
                members
                        .findAllByTenantIdAndUserIdAndMembershipStatus(
                                tenantId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        List<ConnectSpace> result =
                service.listMySpaces();

        assertTrue(
                result.isEmpty()
        );

        verify(
                identity
        ).requireTenantId();

        verify(
                identity
        ).requireUserId();

        verify(
                members
        ).findAllByTenantIdAndUserIdAndMembershipStatus(
                tenantId,
                currentUserId,
                ConnectMembershipStatus.ACTIVE
        );

        verifyNoInteractions(
                spaces
        );
    }


    private void stubTenant() {
        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );
    }

    private void stubTenantAndUser() {
        stubTenant();

        when(
                identity.requireUserId()
        ).thenReturn(
                currentUserId
        );
    }
}
