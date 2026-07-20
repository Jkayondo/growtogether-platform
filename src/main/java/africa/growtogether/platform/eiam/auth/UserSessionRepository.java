package africa.growtogether.platform.eiam.auth;
import java.time.Instant; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.transaction.annotation.Transactional;
public interface UserSessionRepository extends JpaRepository<UserSession,UUID>{
 Optional<UserSession> findByRefreshTokenHash(String hash); long deleteByExpiresAtBefore(Instant cutoff);
 @Modifying @Transactional @Query("update UserSession s set s.revokedAt=:now, s.revokeReason=:reason where s.tenantId=:tenant and s.userId=:user and s.revokedAt is null")
 int revokeAllForUser(@Param("tenant") UUID tenant,@Param("user") UUID user,@Param("reason") String reason,@Param("now") Instant now);
}
