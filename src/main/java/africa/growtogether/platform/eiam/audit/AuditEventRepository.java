package africa.growtogether.platform.eiam.audit;
import java.time.Instant; import java.util.UUID;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEventRepository extends JpaRepository<AuditEvent,UUID> {
 Page<AuditEvent> findByTenantIdAndOccurredAtBetween(UUID tenantId, Instant from, Instant to, Pageable pageable);
 Page<AuditEvent> findByTenantIdAndEventTypeAndOccurredAtBetween(UUID tenantId,String eventType,Instant from,Instant to,Pageable pageable);
 Page<AuditEvent> findByTenantIdAndActorUserIdAndOccurredAtBetween(UUID tenantId,UUID actorUserId,Instant from,Instant to,Pageable pageable);
}
