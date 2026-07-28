package africa.growtogether.platform.school.academic.profile;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class LearnerAcademicProfileService {


    private final LearnerAcademicProfileRepository repository;


    public LearnerAcademicProfileService(
            LearnerAcademicProfileRepository repository
    ) {

        this.repository = repository;
    }


    @Transactional
    public LearnerAcademicProfile create(
            UUID tenantId,
            UUID learnerId
    ) {


        LearnerAcademicProfile profile =
                new LearnerAcademicProfile(
                        learnerId
                );


        profile.setTenantId(
                tenantId
        );


        return repository.save(
                profile
        );
    }



    @Transactional(readOnly = true)
    public LearnerAcademicProfile findByLearner(
            UUID tenantId,
            UUID learnerId
    ) {


        return repository
                .findByTenantIdAndLearnerId(
                        tenantId,
                        learnerId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Learner academic profile not found"
                        )
                );
    }



    @Transactional(readOnly = true)
    public List<LearnerAcademicProfile> findByClassGrade(
            UUID tenantId,
            UUID classGradeId
    ) {


        return repository
                .findByTenantIdAndCurrentClassGradeId(
                        tenantId,
                        classGradeId
                );
    }



    @Transactional(readOnly = true)
    public List<LearnerAcademicProfile> findByCurriculumVersion(
            UUID tenantId,
            UUID curriculumVersionId
    ) {


        return repository
                .findByTenantIdAndCurrentCurriculumVersionId(
                        tenantId,
                        curriculumVersionId
                );
    }



    @Transactional(readOnly = true)
    public List<LearnerAcademicProfile> findActiveProfiles(
            UUID tenantId
    ) {


        return repository
                .findByTenantIdAndAcademicStatus(
                        tenantId,
                        "ACTIVE"
                );
    }



    @Transactional
    public LearnerAcademicProfile archive(
            LearnerAcademicProfile profile
    ) {


        profile.archive();


        return repository.save(
                profile
        );
    }

}
