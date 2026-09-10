package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class CompetencyDescriptorService {


    private final CompetencyDescriptorRepository repository;


    public CompetencyDescriptorService(
            CompetencyDescriptorRepository repository
    ) {

        this.repository = repository;

    }



    public CompetencyDescriptor create(
            UUID tenantId,
            CompetencyDescriptor descriptor
    ) {


        if (tenantId == null) {

            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );

        }


        if (
                descriptor.getDescriptorCode() == null
                || descriptor.getDescriptorCode().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Descriptor code is required"
            );

        }


        if (
                repository.existsByTenantIdAndGradingSchemeIdAndDescriptorCode(
                        tenantId,
                        descriptor.getGradingSchemeId(),
                        descriptor.getDescriptorCode()
                )
        ) {

            throw new IllegalArgumentException(
                    "Competency descriptor already exists"
            );

        }


        descriptor.setTenantId(
                tenantId
        );


        return repository.save(
                descriptor
        );

    }



    @Transactional(readOnly = true)
    public List<CompetencyDescriptor> findByScheme(
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return repository
                .findByTenantIdAndGradingSchemeIdOrderBySequenceNumberAsc(
                        tenantId,
                        gradingSchemeId
                );

    }



    @Transactional(readOnly = true)
    public CompetencyDescriptor findByCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String descriptorCode
    ) {

        return repository
                .findByTenantIdAndGradingSchemeIdAndDescriptorCode(
                        tenantId,
                        gradingSchemeId,
                        descriptorCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Competency descriptor not found"
                        )
                );

    }



    public CompetencyDescriptor archive(
            UUID tenantId,
            UUID gradingSchemeId,
            String descriptorCode
    ) {

        CompetencyDescriptor descriptor =
                findByCode(
                        tenantId,
                        gradingSchemeId,
                        descriptorCode
                );


        descriptor.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );


        return repository.save(
                descriptor
        );

    }

}
