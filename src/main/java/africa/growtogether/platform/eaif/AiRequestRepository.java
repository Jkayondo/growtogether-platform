package africa.growtogether.platform.eaif;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiRequestRepository
        extends JpaRepository<AiRequest, UUID> {

    Optional<AiRequest> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );
}
