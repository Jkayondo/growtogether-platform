package africa.growtogether.platform.school.academic.learner360;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface Learner360ProfileRepository
        extends JpaRepository<Learner360Profile, UUID> {


    Optional<Learner360Profile>
    findByTenantIdAndLearnerId(
            UUID tenantId,
            UUID learnerId
    );


    Optional<Learner360Profile>
    findByTenantIdAndAcademicProfileId(
            UUID tenantId,
            UUID academicProfileId
    );


    List<Learner360Profile>
    findByTenantIdAndCurrentClassGradeId(
            UUID tenantId,
            UUID currentClassGradeId
    );


    List<Learner360Profile>
    findByTenantIdAndCurrentCurriculumVersionId(
            UUID tenantId,
            UUID currentCurriculumVersionId
    );


    List<Learner360Profile>
    findByTenantIdAndLearningRiskLevel(
            UUID tenantId,
            String learningRiskLevel
    );

}
