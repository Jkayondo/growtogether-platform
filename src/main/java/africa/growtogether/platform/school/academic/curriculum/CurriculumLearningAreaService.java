package africa.growtogether.platform.school.academic.curriculum;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class CurriculumLearningAreaService {


    private final CurriculumLearningAreaRepository repository;
    private final CurriculumVersionRepository curriculumVersionRepository;


    public CurriculumLearningAreaService(
            CurriculumLearningAreaRepository repository,
            CurriculumVersionRepository curriculumVersionRepository
    ) {
        this.repository = repository;
        this.curriculumVersionRepository = curriculumVersionRepository;
    }


    @Transactional
    public CurriculumLearningArea create(
            UUID tenantId,
            UUID curriculumVersionId,
            String learningAreaCode,
            String learningAreaName,
            String learningAreaType,
            String description,
            Integer sequenceNumber
    ) {

        curriculumVersionRepository
                .findByTenantIdAndId(
                        tenantId,
                        curriculumVersionId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum version not found"
                        )
                );

        CurriculumLearningArea learningArea =
                new CurriculumLearningArea(
                        curriculumVersionId,
                        learningAreaCode,
                        learningAreaName,
                        learningAreaType,
                        description,
                        sequenceNumber
                );

        learningArea.setTenantId(
                tenantId
        );

        return repository.save(
                learningArea
        );
    }


    @Transactional(readOnly = true)
    public List<CurriculumLearningArea> findByVersion(
            UUID tenantId,
            UUID curriculumVersionId
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdOrderBySequenceNumberAsc(
                        tenantId,
                        curriculumVersionId
                );
    }


    @Transactional(readOnly = true)
    public CurriculumLearningArea findByCode(
            UUID tenantId,
            UUID curriculumVersionId,
            String code
    ) {

        return repository
                .findByTenantIdAndCurriculumVersionIdAndLearningAreaCode(
                        tenantId,
                        curriculumVersionId,
                        code
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Learning area not found"
                        )
                );
    }


    @Transactional
    public CurriculumLearningArea activate(
            CurriculumLearningArea learningArea
    ) {

        learningArea.activate();

        return repository.save(
                learningArea
        );
    }


    @Transactional
    public CurriculumLearningArea deactivate(
            CurriculumLearningArea learningArea
    ) {

        learningArea.deactivate();

        return repository.save(
                learningArea
        );
    }
}
