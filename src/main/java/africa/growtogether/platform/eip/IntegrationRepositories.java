package africa.growtogether.platform.eip;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface IntegrationRouteRepository
    extends JpaRepository<IntegrationRoute, UUID> {

    List<IntegrationRoute> findByTenantIdOrderByPriorityAsc(
        UUID tenantId
    );
}

interface IntegrationCircuitRepository
    extends JpaRepository<IntegrationCircuit, UUID> {

    Optional<IntegrationCircuit> findByTenantIdAndDestination(
        UUID tenantId,
        String destination
    );
}