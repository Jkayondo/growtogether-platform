package africa.growtogether.platform.eiam.authorization;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthorizationPolicyRepository extends JpaRepository<AuthorizationPolicy,UUID>{
 Optional<AuthorizationPolicy> findByTenantIdAndId(UUID tenantId,UUID id);
 boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId,String code);
 List<AuthorizationPolicy> findAllByTenantIdOrderByPriorityDescCodeAsc(UUID tenantId);
 List<AuthorizationPolicy> findAllByTenantIdAndActiveTrueAndResourceTypeIgnoreCaseAndActionIgnoreCaseOrderByPriorityDescCodeAsc(UUID tenantId,String resourceType,String action);
}
