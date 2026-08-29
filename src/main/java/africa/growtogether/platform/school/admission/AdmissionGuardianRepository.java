package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdmissionGuardianRepository
        extends JpaRepository<AdmissionGuardian, UUID> {

    Optional<AdmissionGuardian> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    List<AdmissionGuardian>
    findByTenantIdAndAdmissionApplicationId(
            UUID tenantId,
            UUID admissionApplicationId
    );

    boolean existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
            UUID tenantId,
            UUID admissionApplicationId,
            EntityStatus status
    );
}
