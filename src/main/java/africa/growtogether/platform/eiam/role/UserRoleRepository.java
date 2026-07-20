package africa.growtogether.platform.eiam.role;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findAllByTenantIdAndUserId(UUID tenantId, UUID userId);
    long countByTenantIdAndRoleId(UUID tenantId, UUID roleId);
    void deleteByTenantIdAndUserIdAndRoleId(UUID tenantId, UUID userId, UUID roleId);
    void deleteAllByTenantIdAndUserId(UUID tenantId, UUID userId);
}
