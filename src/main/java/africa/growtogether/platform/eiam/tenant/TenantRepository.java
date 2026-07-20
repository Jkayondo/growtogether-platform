package africa.growtogether.platform.eiam.tenant;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface TenantRepository extends JpaRepository<Tenant,UUID>{ boolean existsByCodeIgnoreCase(String code); List<Tenant> findAllByOrganizationIdOrderByNameAsc(UUID organizationId); }
