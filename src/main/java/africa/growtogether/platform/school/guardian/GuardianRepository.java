package africa.growtogether.platform.school.guardian;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository
        extends JpaRepository<Guardian, UUID> {

    boolean existsByTenantIdAndGuardianNumber(
            UUID tenantId,
            String guardianNumber
    );

    Optional<Guardian> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantIdAndSourceAdmissionGuardianId(
            UUID tenantId,
            UUID sourceAdmissionGuardianId
    );

    Optional<Guardian> findByTenantIdAndSourceAdmissionGuardianId(
            UUID tenantId,
            UUID sourceAdmissionGuardianId
    );

    List<Guardian> findAllByTenantIdAndEiamUserId(
            UUID tenantId,
            UUID eiamUserId
    );

    /*
     * Preserved because existing parent/authorization capabilities
     * may depend on this aggregate query.
     */
    long countByIdIn(
            Collection<UUID> ids
    );
}
