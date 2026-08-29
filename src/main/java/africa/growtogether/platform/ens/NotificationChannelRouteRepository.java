package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.persistence.EntityStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRouteRepository
        extends JpaRepository<NotificationChannelRoute, UUID> {

    Optional<NotificationChannelRoute> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<NotificationChannelRoute>
            findByTenantIdAndChannelAndConnectorId(
                    UUID tenantId,
                    NotificationChannel channel,
                    UUID connectorId
            );

    List<NotificationChannelRoute>
            findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                    UUID tenantId,
                    NotificationChannel channel,
                    EntityStatus status
            );
    boolean existsByTenantIdAndChannelAndPriority(
            UUID tenantId,
            NotificationChannel channel,
            int priority
    );

    List<NotificationChannelRoute>
            findByTenantIdOrderByChannelAscPriorityAsc(
                    UUID tenantId
            );

}
