package africa.growtogether.platform.security.intelligence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecurityIncidentRepository
        extends JpaRepository<SecurityIncident, UUID> {


    Page<SecurityIncident> findByTenantId(
            UUID tenantId,
            Pageable pageable
    );


    Page<SecurityIncident> findByTenantIdAndStatus(
            UUID tenantId,
            SecurityIncidentStatus status,
            Pageable pageable
    );


    Page<SecurityIncident> findByTenantIdAndAssignedTo(
            UUID tenantId,
            UUID assignedTo,
            Pageable pageable
    );
}
