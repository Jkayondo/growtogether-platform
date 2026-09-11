package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CurriculumRepository
        extends JpaRepository<Curriculum, UUID> {


    Optional<Curriculum> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<Curriculum> findByTenantIdAndCurriculumCode(
            UUID tenantId,
            String curriculumCode
    );


    List<Curriculum> findByTenantIdAndCurriculumStatus(
            UUID tenantId,
            String curriculumStatus
    );


    List<Curriculum> findByTenantIdOrderByCurriculumNameAsc(
            UUID tenantId
    );


    List<Curriculum> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
