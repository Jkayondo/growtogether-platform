package africa.growtogether.platform.school.curriculum;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class CurriculumConfigurationService {


    private final CurriculumConfigurationRepository repository;


    public CurriculumConfigurationService(
            CurriculumConfigurationRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public CurriculumConfiguration create(
            UUID tenantId,
            UUID schoolConfigurationId,
            CurriculumType curriculumType,
            String curriculumName,
            String countryCode
    ) {


        if (repository.existsBySchoolConfigurationId(
                schoolConfigurationId
        )) {

            throw new IllegalStateException(
                    "Curriculum configuration already exists."
            );
        }


        CurriculumConfiguration configuration =
                new CurriculumConfiguration(
                        tenantId,
                        schoolConfigurationId,
                        curriculumType,
                        curriculumName,
                        countryCode
                );


        return repository.save(configuration);
    }


    @Transactional(readOnly = true)
    public CurriculumConfiguration getBySchool(
            UUID schoolConfigurationId
    ) {

        return repository
                .findBySchoolConfigurationId(
                        schoolConfigurationId
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Curriculum configuration not found."
                        )
                );
    }
}
