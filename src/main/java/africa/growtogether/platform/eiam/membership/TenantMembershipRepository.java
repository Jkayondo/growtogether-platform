package africa.growtogether.platform.eiam.membership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, UUID> {
    Optional<TenantMembership> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<TenantMembership> findByTenantIdAndUserId(UUID tenantId, UUID userId);
    List<TenantMembership> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
