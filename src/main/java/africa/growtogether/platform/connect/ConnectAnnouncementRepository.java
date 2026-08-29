package africa.growtogether.platform.connect;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectAnnouncementRepository
        extends JpaRepository<ConnectAnnouncement, UUID> {

    Optional<ConnectAnnouncement>
    findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<ConnectAnnouncement>
    findByTenantIdAndMessageId(
            UUID tenantId,
            UUID messageId
    );

    List<ConnectAnnouncement>
    findAllByTenantIdAndSpaceIdOrderByPublishedAtDesc(
            UUID tenantId,
            UUID spaceId
    );
}
