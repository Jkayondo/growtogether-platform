package africa.growtogether.platform.school.academic.learner360;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class Learner360ProfileService {


    private final Learner360ProfileRepository repository;


    public Learner360ProfileService(
            Learner360ProfileRepository repository
    ) {

        this.repository = repository;
    }



    @Transactional
    public Learner360Profile create(
            UUID tenantId,
            UUID learnerId,
            UUID academicProfileId
    ) {


        Learner360Profile profile =
                new Learner360Profile(
                        learnerId,
                        academicProfileId
                );


        profile.setTenantId(
                tenantId
        );


        return repository.save(
                profile
        );
    }



    @Transactional(readOnly = true)
    public Learner360Profile findByLearner(
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
                                "Learner 360 profile not found"
                        )
                );
    }



    @Transactional(readOnly = true)
    public List<Learner360Profile> findByClass(
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
    public List<Learner360Profile> findByCurriculum(
            UUID tenantId,
            UUID curriculumVersionId
    ) {


        return repository
                .findByTenantIdAndCurrentCurriculumVersionId(
                        tenantId,
                        curriculumVersionId
                );
    }



    @Transactional
    public Learner360Profile updateLearningRisk(
            Learner360Profile profile,
            String riskLevel
    ) {


        profile.updateLearningRiskLevel(
                riskLevel
        );


        return repository.save(
                profile
        );
    }



    @Transactional
    public Learner360Profile updateGrowthSummary(
            Learner360Profile profile,
            String summary
    ) {


        profile.updateGrowthSummary(
                summary
        );


        return repository.save(
                profile
        );
    }



    @Transactional
    public Learner360Profile archive(
            Learner360Profile profile
    ) {


        profile.archive();


        return repository.save(
                profile
        );
    }

}
