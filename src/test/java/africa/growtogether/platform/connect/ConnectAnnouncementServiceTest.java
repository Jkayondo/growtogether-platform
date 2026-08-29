package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConnectAnnouncementServiceTest {

    private ConnectSpaceRepository spaces;
    private ConnectMessageRepository messages;
    private ConnectAnnouncementRepository announcements;
    private EnterpriseIdentityContext identity;

    private ConnectAnnouncementService service;

    @BeforeEach
    void setUp() {

        spaces =
                mock(
                        ConnectSpaceRepository.class
                );

        messages =
                mock(
                        ConnectMessageRepository.class
                );

        announcements =
                mock(
                        ConnectAnnouncementRepository.class
                );

        identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        service =
                new ConnectAnnouncementService(
                        spaces,
                        messages,
                        announcements,
                        identity
                );
    }

    @Test
    void authorisedPublisherCanPublishToGroupAudience() {

        UUID tenantId =
                UUID.randomUUID();

        UUID publisherUserId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        ConnectSpace space =
                space(
                        spaceId,
                        ConnectSpaceType.GROUP
                );

        when(
                identity.hasPermission(
                        ConnectPermissions.SEND_ANNOUNCEMENT
                )
        ).thenReturn(
                true
        );

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                publisherUserId
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
                messages.saveAndFlush(
                        any(
                                ConnectMessage.class
                        )
                )
        ).thenAnswer(
                invocation -> {
                    ConnectMessage message =
                            invocation.getArgument(
                                    0
                            );

                    return messageWithId(
                            message,
                            messageId
                    );
                }
        );

        when(
                announcements.save(
                        any(
                                ConnectAnnouncement.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        ConnectAnnouncement result =
                service.publish(
                        spaceId,
                        "  Important Notice  ",
                        "  School closes at 1:00 PM.  "
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                spaceId,
                result.getSpaceId()
        );

        assertEquals(
                messageId,
                result.getMessageId()
        );

        assertEquals(
                publisherUserId,
                result.getPublishedByUserId()
        );

        assertEquals(
                "Important Notice",
                result.getTitle()
        );

        assertEquals(
                "School closes at 1:00 PM.",
                result.getBody()
        );

        verify(
                messages
        ).saveAndFlush(
                argThat(
                        message ->
                                message.getMessageType()
                                        == ConnectMessageType.SYSTEM
                                        && publisherUserId.equals(
                                        message.getSenderUserId()
                                )
                                        && spaceId.equals(
                                        message.getSpaceId()
                                )
                                        && message.getBody()
                                        .equals(
                                                "Important Notice\n\nSchool closes at 1:00 PM."
                                        )
                )
        );
    }

    @Test
    void publisherWithoutAnnouncementPermissionIsDenied() {

        when(
                identity.hasPermission(
                        ConnectPermissions.SEND_ANNOUNCEMENT
                )
        ).thenReturn(
                false
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.publish(
                        UUID.randomUUID(),
                        "Notice",
                        "Body"
                )
        );

        verifyNoInteractions(
                spaces,
                messages,
                announcements
        );
    }

    @Test
    void tenantScopedAudienceLookupIsRequired() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.SEND_ANNOUNCEMENT
                )
        ).thenReturn(
                true
        );

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.publish(
                        spaceId,
                        "Notice",
                        "Body"
                )
        );

        verifyNoInteractions(
                messages,
                announcements
        );
    }

    @Test
    void directSpaceCannotBeAnnouncementAudience() {

        assertAudienceDenied(
                ConnectSpaceType.DIRECT
        );
    }

    @Test
    void systemSpaceCannotBeAnnouncementAudience() {

        assertAudienceDenied(
                ConnectSpaceType.SYSTEM
        );
    }

    @Test
    void classDepartmentAndInstitutionAreValidAudiences() {

        assertAudienceAccepted(
                ConnectSpaceType.CLASS
        );

        assertAudienceAccepted(
                ConnectSpaceType.DEPARTMENT
        );

        assertAudienceAccepted(
                ConnectSpaceType.INSTITUTION
        );
    }

    private void assertAudienceDenied(
            ConnectSpaceType type
    ) {

        reset(
                spaces,
                messages,
                announcements,
                identity
        );

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.SEND_ANNOUNCEMENT
                )
        ).thenReturn(
                true
        );

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                UUID.randomUUID()
        );

        ConnectSpace audienceSpace =
                space(
                        spaceId,
                        type
                );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        audienceSpace
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () -> service.publish(
                        spaceId,
                        "Notice",
                        "Body"
                )
        );

        verifyNoInteractions(
                messages,
                announcements
        );
    }

    private void assertAudienceAccepted(
            ConnectSpaceType type
    ) {

        reset(
                spaces,
                messages,
                announcements,
                identity
        );

        UUID tenantId =
                UUID.randomUUID();

        UUID publisherUserId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        when(
                identity.hasPermission(
                        ConnectPermissions.SEND_ANNOUNCEMENT
                )
        ).thenReturn(
                true
        );

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                publisherUserId
        );

        ConnectSpace audienceSpace =
                space(
                        spaceId,
                        type
                );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        audienceSpace
                )
        );

        when(
                messages.saveAndFlush(
                        any(
                                ConnectMessage.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        messageWithId(
                                invocation.getArgument(
                                        0
                                ),
                                messageId
                        )
        );

        when(
                announcements.save(
                        any(
                                ConnectAnnouncement.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        assertDoesNotThrow(
                () -> service.publish(
                        spaceId,
                        "Notice",
                        "Body"
                )
        );
    }

    private ConnectSpace space(
            UUID id,
            ConnectSpaceType type
    ) {

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                id
        );

        when(
                space.getSpaceType()
        ).thenReturn(
                type
        );

        return space;
    }

    private ConnectMessage messageWithId(
            ConnectMessage original,
            UUID id
    ) {

        ConnectMessage saved =
                mock(
                        ConnectMessage.class
                );

        when(
                saved.getId()
        ).thenReturn(
                id
        );

        when(
                saved.getSpaceId()
        ).thenReturn(
                original.getSpaceId()
        );

        when(
                saved.getSenderUserId()
        ).thenReturn(
                original.getSenderUserId()
        );

        when(
                saved.getMessageType()
        ).thenReturn(
                original.getMessageType()
        );

        when(
                saved.getBody()
        ).thenReturn(
                original.getBody()
        );

        return saved;
    }
}
