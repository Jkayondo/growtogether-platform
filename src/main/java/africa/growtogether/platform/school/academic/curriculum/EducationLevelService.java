package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class EducationLevelService {


    private final EducationLevelRepository repository;


    public EducationLevelService(
            EducationLevelRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public EducationLevel create(
            UUID tenantId,
            String levelCode,
            String levelName,
            String description,
            Integer sequenceNumber
    ) {

        EducationLevel level =
                new EducationLevel(
                        levelCode,
                        levelName,
                        description,
                        sequenceNumber
                );


        level.setTenantId(
                tenantId
        );


        return repository.save(
                level
        );
    }


    @Transactional(readOnly = true)
    public EducationLevel get(
            UUID tenantId,
            UUID id
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Education level not found for tenant"
                        )
                );
    }


    @Transactional(readOnly = true)
    public EducationLevel findByCode(
            UUID tenantId,
            String levelCode
    ) {

        return repository
                .findByTenantIdAndLevelCode(
                        tenantId,
                        levelCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Education level not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<EducationLevel> findAll(
            UUID tenantId
    ) {

        return repository
                .findByTenantIdOrderBySequenceNumberAsc(
                        tenantId
                );
    }


    @Transactional(readOnly = true)
    public List<EducationLevel> findByStatus(
            UUID tenantId,
            String status
    ) {

        return repository
                .findByTenantIdAndStatus(
                        tenantId,
                        status
                );
    }


    @Transactional
    public EducationLevel activate(
            EducationLevel level
    ) {

        level.activate();

        return repository.save(
                level
        );
    }


    @Transactional
    public EducationLevel deactivate(
            EducationLevel level
    ) {

        level.deactivate();

        return repository.save(
                level
        );
    }

}
