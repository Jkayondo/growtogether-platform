package africa.growtogether.platform.eiam.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID>, JpaSpecificationExecutor<UserAccount> {
    boolean existsByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);
    boolean existsByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
    Optional<UserAccount> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<UserAccount> findByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);
    Optional<UserAccount> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
}
