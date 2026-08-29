package africa.growtogether.platform.connect;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectMessageReceiptRepository
        extends JpaRepository<ConnectMessageReceipt, UUID> {

    Optional<ConnectMessageReceipt>
    findByTenantIdAndMessageIdAndUserId(
            UUID tenantId,
            UUID messageId,
            UUID userId
    );

    List<ConnectMessageReceipt>
    findAllByTenantIdAndMessageId(
            UUID tenantId,
            UUID messageId
    );

    List<ConnectMessageReceipt>
    findAllByTenantIdAndUserIdAndReadAtIsNull(
            UUID tenantId,
            UUID userId
    );
}
