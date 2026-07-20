package africa.growtogether.platform.eiam.recovery;

import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, UUID> {
    Optional<RecoveryToken> findByTokenHashAndPurpose(String tokenHash, RecoveryTokenPurpose purpose);
    List<RecoveryToken> findAllByTenantIdAndUserIdAndPurposeAndConsumedAtIsNullAndInvalidatedAtIsNull(UUID tenantId, UUID userId, RecoveryTokenPurpose purpose);
    long deleteByExpiresAtBefore(Instant cutoff);
}
