package africa.growtogether.platform.eiam.mfa;
import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.transaction.annotation.Transactional;
interface MfaProfileRepository extends JpaRepository<MfaProfile,UUID>{Optional<MfaProfile> findByTenantIdAndUserId(UUID tenantId,UUID userId);}
interface MfaChallengeRepository extends JpaRepository<MfaChallenge,UUID>{Optional<MfaChallenge> findByChallengeHash(String hash);}
interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode,UUID>{Optional<MfaRecoveryCode> findByTenantIdAndUserIdAndCodeHash(UUID tenantId,UUID userId,String hash); @Modifying @Transactional @Query("delete from MfaRecoveryCode c where c.tenantId=:tenant and c.userId=:user") int deleteForUser(@Param("tenant")UUID tenant,@Param("user")UUID user);}
interface TrustedDeviceRepository extends JpaRepository<TrustedDevice,UUID>{Optional<TrustedDevice> findByTokenHash(String hash); List<TrustedDevice> findAllByTenantIdAndUserIdOrderByCreatedAtDesc(UUID tenantId,UUID userId); Optional<TrustedDevice> findByIdAndTenantIdAndUserId(UUID id,UUID tenantId,UUID userId);}
