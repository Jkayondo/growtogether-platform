package africa.growtogether.platform.school.admission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdmissionGuardianAccountProvisioningRepository
        extends JpaRepository<AdmissionGuardianAccountProvisioning, UUID> {

    Optional<AdmissionGuardianAccountProvisioning>
    findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<AdmissionGuardianAccountProvisioning>
    findByTenantIdAndAdmissionGuardianId(
            UUID tenantId,
            UUID admissionGuardianId
    );

    Optional<AdmissionGuardianAccountProvisioning>
    findByTenantIdAndInvitationId(
            UUID tenantId,
            UUID invitationId
    );

    List<AdmissionGuardianAccountProvisioning>
    findByTenantIdAndEiamUserId(
            UUID tenantId,
            UUID eiamUserId
    );

    boolean existsByTenantIdAndAdmissionGuardianId(
            UUID tenantId,
            UUID admissionGuardianId
    );
}
