package africa.growtogether.platform.ens;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRequestRepository
        extends JpaRepository<NotificationRequest, UUID> {

    Optional<NotificationRequest> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select n from NotificationRequest n "
                    + "where n.id = :id "
                    + "and n.tenantId = :tenantId"
    )
    Optional<NotificationRequest> findByIdAndTenantIdForUpdate(
            @Param("id") UUID id,
            @Param("tenantId") UUID tenantId
    );
}
