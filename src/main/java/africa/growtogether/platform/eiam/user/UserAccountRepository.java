package africa.growtogether.platform.eiam.user;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID>, JpaSpecificationExecutor<UserAccount> {
    boolean existsByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);
    boolean existsByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
    long countByTenantId(UUID tenantId);
    Optional<UserAccount> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<UserAccount> findByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);
    Optional<UserAccount> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
    Optional<UserAccount> findByTenantIdAndPrimaryPhoneNumber(
            UUID tenantId,
            String primaryPhoneNumber
    );
    Optional<UserAccount> findByTenantIdAndPrimaryPhoneNumberAndPhoneVerifiedAtIsNotNull(
            UUID tenantId,
            String primaryPhoneNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from UserAccount u
            where u.id = :id
              and u.tenantId = :tenantId
            """)
    Optional<UserAccount> findByIdAndTenantIdForAuthenticationSecurityUpdate(
            @Param("id") UUID id,
            @Param("tenantId") UUID tenantId
    );

}
