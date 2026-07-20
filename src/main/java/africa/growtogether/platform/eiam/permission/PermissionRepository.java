package africa.growtogether.platform.eiam.permission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Permission> findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);
    List<Permission> findAllByTenantIdOrderByModuleAscCodeAsc(UUID tenantId);
    List<Permission> findAllByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);
}
