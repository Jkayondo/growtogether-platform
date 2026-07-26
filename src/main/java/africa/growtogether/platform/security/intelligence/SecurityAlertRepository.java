package africa.growtogether.platform.security.intelligence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlert, UUID> {


    Page<SecurityAlert> findByTenantId(
            UUID tenantId,
            Pageable pageable
    );


    Page<SecurityAlert> findByTenantIdAndAlertStatus(
            UUID tenantId,
            AlertStatus alertStatus,
            Pageable pageable
    );


    Page<SecurityAlert> findByTenantIdAndSeverity(
            UUID tenantId,
            SecurityRiskAssessment.RiskLevel severity,
            Pageable pageable
    );
}
