package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class CurriculumVersionService {


    private final CurriculumVersionRepository repository;


    public CurriculumVersionService(
            CurriculumVersionRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public CurriculumVersion create(
            UUID tenantId,
            Curriculum curriculum,
            String versionCode,
            String versionName,
            LocalDate effectiveFrom
    ) {


        CurriculumVersion version =
                new CurriculumVersion(
                        curriculum,
                        versionCode,
                        versionName,
                        effectiveFrom
                );


        version.setTenantId(
                tenantId
        );


        return repository.save(
                version
        );
    }


    @Transactional(readOnly = true)
    public List<CurriculumVersion> findByCurriculum(
            UUID tenantId,
            UUID curriculumId
    ) {

        return repository
                .findByTenantIdAndCurriculumId(
                        tenantId,
                        curriculumId
                );
    }


    @Transactional(readOnly = true)
    public CurriculumVersion findByCode(
            UUID tenantId,
            UUID curriculumId,
            String versionCode
    ) {

        return repository
                .findByTenantIdAndCurriculumIdAndVersionCode(
                        tenantId,
                        curriculumId,
                        versionCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Curriculum version not found"
                        )
                );
    }


    @Transactional
    public CurriculumVersion approve(
            CurriculumVersion version,
            UUID approvedBy,
            String approvalReference
    ) {

        version.approve(
                approvedBy,
                approvalReference
        );


        return repository.save(
                version
        );
    }


    @Transactional
    public CurriculumVersion activate(
            CurriculumVersion version
    ) {

        version.activate();


        return repository.save(
                version
        );
    }

}
