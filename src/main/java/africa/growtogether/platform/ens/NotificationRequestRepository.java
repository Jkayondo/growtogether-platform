package africa.growtogether.platform.ens;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRequestRepository extends JpaRepository<NotificationRequest,UUID> { Optional<NotificationRequest> findByIdAndTenantId(UUID id, UUID tenantId); }
