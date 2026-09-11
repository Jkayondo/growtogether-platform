package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface EducationLevelRepository
        extends JpaRepository<EducationLevel, UUID> {


    Optional<EducationLevel> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<EducationLevel> findByTenantIdAndLevelCode(
            UUID tenantId,
            String levelCode
    );


    List<EducationLevel> findByTenantIdOrderBySequenceNumberAsc(
            UUID tenantId
    );


    List<EducationLevel> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
