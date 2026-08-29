package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConnectAnnouncementService {

    private final ConnectSpaceRepository spaces;
    private final ConnectMessageRepository messages;
    private final ConnectAnnouncementRepository announcements;
    private final EnterpriseIdentityContext identity;

    public ConnectAnnouncementService(
            ConnectSpaceRepository spaces,
            ConnectMessageRepository messages,
            ConnectAnnouncementRepository announcements,
            EnterpriseIdentityContext identity
    ) {
        this.spaces = spaces;
        this.messages = messages;
        this.announcements = announcements;
        this.identity = identity;
    }

    @Transactional
    public ConnectAnnouncement publish(
            UUID spaceId,
            String title,
            String body
    ) {

        requireAnnouncementPermission();

        UUID tenantId =
                identity.requireTenantId();

        UUID publisherUserId =
                identity.requireUserId();

        ConnectSpace space =
                spaces
                        .findByIdAndTenantId(
                                spaceId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "GT Connect announcement audience was not found"
                                )
                        );

        requireAnnouncementAudience(
                space
        );

        String cleanTitle =
                requiredText(
                        title,
                        "title"
                );

        String cleanBody =
                requiredText(
                        body,
                        "body"
                );

        /*
         * The SYSTEM message is a conversation projection.
         * The announcement record remains the authoritative
         * publication record.
         */
        ConnectMessage projection =
                new ConnectMessage(
                        tenantId,
                        spaceId,
                        publisherUserId,
                        ConnectMessageType.SYSTEM,
                        cleanTitle
                                + "\n\n"
                                + cleanBody,
                        null
                );

        /*
         * Flush first because the announcement table has a
         * database FK to the projected message. Both operations
         * remain inside this transaction, so a later failure
         * still rolls everything back.
         */
        ConnectMessage savedProjection =
                messages.saveAndFlush(
                        projection
                );

        ConnectAnnouncement announcement =
                new ConnectAnnouncement(
                        tenantId,
                        spaceId,
                        savedProjection.getId(),
                        publisherUserId,
                        cleanTitle,
                        cleanBody
                );

        return announcements.save(
                announcement
        );
    }

    private void requireAnnouncementPermission() {

        if (
                !identity.hasPermission(
                        ConnectPermissions.SEND_ANNOUNCEMENT
                )
        ) {
            throw new AccessDeniedException(
                    "Official GT Connect announcement publication requires core.announcements.send"
            );
        }
    }

    private void requireAnnouncementAudience(
            ConnectSpace space
    ) {

        ConnectSpaceType type =
                space.getSpaceType();

        if (
                type == ConnectSpaceType.GROUP
                        || type == ConnectSpaceType.CLASS
                        || type == ConnectSpaceType.DEPARTMENT
                        || type == ConnectSpaceType.INSTITUTION
        ) {
            return;
        }

        throw new AccessDeniedException(
                "This GT Connect space type cannot be used as a Release 1 announcement audience"
        );
    }

    private static String requiredText(
            String value,
            String name
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    name + " is required"
            );
        }

        return value.trim();
    }
}
