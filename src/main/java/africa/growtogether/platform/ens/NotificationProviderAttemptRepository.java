package africa.growtogether.platform.ens;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationProviderAttemptRepository
        extends JpaRepository<NotificationProviderAttempt, UUID> {

    Optional<NotificationProviderAttempt> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<NotificationProviderAttempt>
            findByTenantIdAndNotificationRequestIdOrderByAttemptNumberAsc(
                    UUID tenantId,
                    UUID notificationRequestId
            );

    Optional<NotificationProviderAttempt>
            findByTenantIdAndNotificationRequestIdAndAttemptNumber(
                    UUID tenantId,
                    UUID notificationRequestId,
                    int attemptNumber
            );

    Optional<NotificationProviderAttempt>
            findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                    UUID tenantId,
                    UUID notificationRequestId
            );
}
