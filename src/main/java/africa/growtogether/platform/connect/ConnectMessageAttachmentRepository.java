package africa.growtogether.platform.connect;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectMessageAttachmentRepository
        extends JpaRepository<ConnectMessageAttachment, UUID> {

    List<ConnectMessageAttachment>
    findAllByTenantIdAndMessageId(
            UUID tenantId,
            UUID messageId
    );

    List<ConnectMessageAttachment>
    findAllByTenantIdAndMessageIdIn(
            UUID tenantId,
            List<UUID> messageIds
    );


    Optional<ConnectMessageAttachment>
    findByTenantIdAndMessageIdAndDocumentIdAndDocumentVersion(
            UUID tenantId,
            UUID messageId,
            UUID documentId,
            int documentVersion
    );
}
