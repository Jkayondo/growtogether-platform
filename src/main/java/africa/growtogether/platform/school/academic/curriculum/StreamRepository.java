package africa.growtogether.platform.school.academic.curriculum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StreamRepository
        extends JpaRepository<Stream, UUID> {

    Optional<Stream> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<Stream> findByTenantIdAndCampusIdAndClassGradeIdAndStreamCode(
            UUID tenantId,
            UUID campusId,
            UUID classGradeId,
            String streamCode
    );

    List<Stream> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );

    List<Stream> findByTenantIdAndClassGradeId(
            UUID tenantId,
            UUID classGradeId
    );

    List<Stream> findByTenantIdAndCampusIdAndClassGradeId(
            UUID tenantId,
            UUID campusId,
            UUID classGradeId
    );
}
