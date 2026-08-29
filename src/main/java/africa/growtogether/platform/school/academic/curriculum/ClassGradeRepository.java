package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ClassGradeRepository
        extends JpaRepository<ClassGrade, UUID> {

    Optional<ClassGrade> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );



    List<ClassGrade> findByTenantIdAndEducationLevelId(
            UUID tenantId,
            UUID educationLevelId
    );


    Optional<ClassGrade> findByTenantIdAndClassCode(
            UUID tenantId,
            String classCode
    );


    List<ClassGrade> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
