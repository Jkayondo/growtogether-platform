package africa.growtogether.platform.school.academic.profile;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface LearnerAcademicProfileRepository
        extends JpaRepository<LearnerAcademicProfile, UUID> {


    Optional<LearnerAcademicProfile>
    findByTenantIdAndLearnerId(
            UUID tenantId,
            UUID learnerId
    );


    List<LearnerAcademicProfile>
    findByTenantIdAndCurrentClassGradeId(
            UUID tenantId,
            UUID currentClassGradeId
    );


    List<LearnerAcademicProfile>
    findByTenantIdAndCurrentCurriculumVersionId(
            UUID tenantId,
            UUID currentCurriculumVersionId
    );


    List<LearnerAcademicProfile>
    findByTenantIdAndAcademicStatus(
            UUID tenantId,
            String academicStatus
    );

}
