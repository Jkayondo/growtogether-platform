package africa.growtogether.platform.eip;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface ConnectorCertificationRepository extends JpaRepository<ConnectorCertification,UUID>{List<ConnectorCertification> findByTenantIdOrderByCreatedAtDesc(UUID tenantId); Optional<ConnectorCertification> findByTenantIdAndId(UUID tenantId,UUID id);}
