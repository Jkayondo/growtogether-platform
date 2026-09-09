package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;
import africa.growtogether.platform.eiam.user.UserAccountRepository;

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
class ConnectMessageSearchServiceTest {

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
    private UserAccountRepository userAccounts;

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
                        userAccounts,
                        identity,
                        parentAuthorization,
                        teacherAssignmentAuthorization
                );

        tenantId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        spaceId = UUID.randomUUID();

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
    void authorisedMemberSearchesOnlyCurrentTenantAndSpace() {

        stubOrdinarySpaceAccess();

        ConnectMessage result =
                mock(
                        ConnectMessage.class
                );

        when(
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "school fees",
                        25
                )
        ).thenReturn(
                List.of(
                        result
                )
        );

        List<ConnectMessage> found =
                service.searchMessages(
                        spaceId,
                        "  school fees  ",
                        null
                );

        assertEquals(
                List.of(
                        result
                ),
                found
        );

        verify(
                messages
        ).searchConversation(
                tenantId,
                spaceId,
                "school fees",
                25
        );
    }

    @Test
    void requestedSafeLimitIsPassedToRepository() {

        stubOrdinarySpaceAccess();

        when(
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "meeting",
                        40
                )
        ).thenReturn(
                List.of()
        );

        service.searchMessages(
                spaceId,
                "meeting",
                40
        );

        verify(
                messages
        ).searchConversation(
                tenantId,
                spaceId,
                "meeting",
                40
        );
    }

    @Test
    void blankQueryIsRejectedBeforeRepositorySearch() {

        stubOrdinarySpaceAccess();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.searchMessages(
                                spaceId,
                                "   ",
                                null
                        )
        );

        verify(
                messages,
                never()
        ).searchConversation(
                any(),
                any(),
                anyString(),
                anyInt()
        );
    }

    @Test
    void oversizedQueryIsRejected() {

        stubOrdinarySpaceAccess();

        String query =
                "x".repeat(
                        201
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.searchMessages(
                                spaceId,
                                query,
                                null
                        )
        );

        verify(
                messages,
                never()
        ).searchConversation(
                any(),
                any(),
                anyString(),
                anyInt()
        );
    }

    @Test
    void unsafeResultLimitsAreRejected() {

        stubOrdinarySpaceAccess();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.searchMessages(
                                spaceId,
                                "fees",
                                0
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.searchMessages(
                                spaceId,
                                "fees",
                                51
                        )
        );

        verify(
                messages,
                never()
        ).searchConversation(
                any(),
                any(),
                anyString(),
                anyInt()
        );
    }

    @Test
    void nonMemberCannotSearchConversation() {

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

        when(
                members.findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
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
                        service.searchMessages(
                                spaceId,
                                "school",
                                null
                        )
        );

        verify(
                messages,
                never()
        ).searchConversation(
                any(),
                any(),
                anyString(),
                anyInt()
        );
    }

    @Test
    void parentSearchRevalidatesLiveLearnerRelationship() {

        UUID studentId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        ConnectSpaceMember membership =
                mock(
                        ConnectSpaceMember.class
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
                membership.getMemberRole()
        ).thenReturn(
                ConnectMemberRole.MEMBER
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

        when(
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "fees",
                        25
                )
        ).thenReturn(
                List.of()
        );

        service.searchMessages(
                spaceId,
                "fees",
                null
        );

        verify(
                parentAuthorization
        ).requireUserCanCommunicateWithStudent(
                currentUserId,
                studentId
        );

        verify(
                messages
        ).searchConversation(
                tenantId,
                spaceId,
                "fees",
                25
        );
    }

    @Test
    void revokedParentRelationshipCannotSearchEvenWithActiveMembership() {

        UUID studentId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        ConnectSpaceMember membership =
                mock(
                        ConnectSpaceMember.class
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
                membership.getMemberRole()
        ).thenReturn(
                ConnectMemberRole.MEMBER
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
                () ->
                        service.searchMessages(
                                spaceId,
                                "fees",
                                null
                        )
        );

        verify(
                messages,
                never()
        ).searchConversation(
                any(),
                any(),
                anyString(),
                anyInt()
        );
    }

    @Test
    void teacherSearchRevalidatesLiveStreamAssignment() {

        UUID streamId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        ConnectSpaceMember membership =
                mock(
                        ConnectSpaceMember.class
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
                membership.getMemberRole()
        ).thenReturn(
                ConnectMemberRole.MEMBER
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

        when(
                messages.searchConversation(
                        tenantId,
                        spaceId,
                        "homework",
                        25
                )
        ).thenReturn(
                List.of()
        );

        service.searchMessages(
                spaceId,
                "homework",
                null
        );

        verify(
                teacherAssignmentAuthorization
        ).requireTeacherCanAccessStream(
                currentUserId,
                streamId
        );

        verify(
                messages
        ).searchConversation(
                tenantId,
                spaceId,
                "homework",
                25
        );
    }

    @Test
    void revokedTeacherAssignmentCannotSearchEvenWithActiveMembership() {

        UUID streamId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        ConnectSpaceMember membership =
                mock(
                        ConnectSpaceMember.class
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
                membership.getMemberRole()
        ).thenReturn(
                ConnectMemberRole.MEMBER
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
                () ->
                        service.searchMessages(
                                spaceId,
                                "homework",
                                null
                        )
        );

        verify(
                messages,
                never()
        ).searchConversation(
                any(),
                any(),
                anyString(),
                anyInt()
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
}
