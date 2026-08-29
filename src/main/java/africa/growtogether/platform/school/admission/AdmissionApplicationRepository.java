package africa.growtogether.platform.school.admission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdmissionApplicationRepository
        extends JpaRepository<AdmissionApplication, UUID> {

    Optional<AdmissionApplication> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<AdmissionApplication> findByTenantIdAndApplicationNumber(
            UUID tenantId,
            String applicationNumber
    );

    boolean existsByTenantIdAndApplicationNumber(
            UUID tenantId,
            String applicationNumber
    );

    List<AdmissionApplication> findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );

    List<AdmissionApplication> findByTenantIdAndAdmissionStatus(
            UUID tenantId,
            String admissionStatus
    );
}
