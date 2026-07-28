package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CurriculumVersionRepository
        extends JpaRepository<CurriculumVersion, UUID> {


    List<CurriculumVersion> findByTenantIdAndCurriculumId(
            UUID tenantId,
            UUID curriculumId
    );


    Optional<CurriculumVersion> findByTenantIdAndCurriculumIdAndVersionCode(
            UUID tenantId,
            UUID curriculumId,
            String versionCode
    );


    List<CurriculumVersion> findByTenantIdAndVersionStatus(
            UUID tenantId,
            String versionStatus
    );


    List<CurriculumVersion> findByTenantIdAndVersionStatusOrderByEffectiveFromDesc(
            UUID tenantId,
            String versionStatus
    );

}
