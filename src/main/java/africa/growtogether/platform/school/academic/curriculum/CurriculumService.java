package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class CurriculumService {


    private final CurriculumRepository repository;


    public CurriculumService(
            CurriculumRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public Curriculum create(
            UUID tenantId,
            String curriculumCode,
            String curriculumName,
            String curriculumType
    ) {

        Curriculum curriculum =
                new Curriculum(
                        curriculumCode,
                        curriculumName,
                        curriculumType
                );


        curriculum.setTenantId(
                tenantId
        );


        return repository.save(
                curriculum
        );
    }


    @Transactional(readOnly = true)
    public Curriculum findByCode(
            UUID tenantId,
            String curriculumCode
    ) {

        return repository
                .findByTenantIdAndCurriculumCode(
                        tenantId,
                        curriculumCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<Curriculum> findActiveCurricula(
            UUID tenantId
    ) {

        return repository
                .findByTenantIdAndCurriculumStatus(
                        tenantId,
                        "ACTIVE"
                );
    }


    @Transactional
    public Curriculum activate(
            UUID tenantId,
            Curriculum curriculum
    ) {

        if (!curriculum.getTenantId()
                .equals(tenantId)) {

            throw new IllegalArgumentException(
                    "Curriculum does not belong to tenant"
            );
        }


        curriculum.activate();


        return repository.save(
                curriculum
        );
    }

}
