package africa.growtogether.platform.eiam.role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Role> findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);
    Optional<Role> findByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
    List<Role> findAllByTenantIdOrderByNameAsc(UUID tenantId);
}
