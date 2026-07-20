package africa.growtogether.platform.eip;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationMessageRepository
    extends JpaRepository<IntegrationMessage, UUID> {

    Optional<IntegrationMessage> findByTenantIdAndId(
        UUID tenantId,
        UUID id
    );

    Optional<IntegrationMessage> findByTenantIdAndIdempotencyKey(
        UUID tenantId,
        String key
    );
}