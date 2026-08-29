package africa.growtogether.platform.school.admission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdmissionApplicantRepository
        extends JpaRepository<AdmissionApplicant, UUID> {

    Optional<AdmissionApplicant> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<AdmissionApplicant>
    findByTenantIdAndAdmissionApplicationId(
            UUID tenantId,
            UUID admissionApplicationId
    );

    boolean existsByTenantIdAndAdmissionApplicationId(
            UUID tenantId,
            UUID admissionApplicationId
    );
}
