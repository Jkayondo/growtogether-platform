package africa.growtogether.platform.eiam.tenant;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface OrganizationRepository extends JpaRepository<Organization,UUID>{ boolean existsByCodeIgnoreCase(String code); }
