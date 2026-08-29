package africa.growtogether.platform.ens;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSecurePayloadRepository
        extends JpaRepository<NotificationSecurePayload, UUID> {

    Optional<NotificationSecurePayload> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<NotificationSecurePayload>
            findByTenantIdAndNotificationRequestId(
                    UUID tenantId,
                    UUID notificationRequestId
            );

    boolean existsByTenantIdAndNotificationRequestId(
            UUID tenantId,
            UUID notificationRequestId
    );
}
