package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class GradingSchemeService {


    private final GradingSchemeRepository repository;


    public GradingSchemeService(
            GradingSchemeRepository repository
    ) {

        this.repository = repository;

    }



    public GradingScheme create(
            UUID tenantId,
            GradingScheme gradingScheme
    ) {


        if (tenantId == null) {

            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );

        }


        if (
                gradingScheme.getSchemeCode() == null
                || gradingScheme.getSchemeCode().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Scheme code is required"
            );

        }


        if (
                repository.existsByTenantIdAndSchemeCode(
                        tenantId,
                        gradingScheme.getSchemeCode()
                )
        ) {

            throw new IllegalArgumentException(
                    "Grading scheme code already exists"
            );

        }


        gradingScheme.setTenantId(
                tenantId
        );


        return repository.save(
                gradingScheme
        );

    }



    @Transactional(readOnly = true)
    public GradingScheme findByCode(
            UUID tenantId,
            String schemeCode
    ) {

        return repository
                .findByTenantIdAndSchemeCode(
                        tenantId,
                        schemeCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Grading scheme not found"
                        )
                );

    }



    @Transactional(readOnly = true)
    public List<GradingScheme> findActive(
            UUID tenantId
    ) {

        return repository
                .findByTenantIdAndStatus(
                        tenantId,
                        "ACTIVE"
                );

    }



    public GradingScheme archive(
            UUID tenantId,
            String schemeCode
    ) {

        GradingScheme scheme =
                findByCode(
                        tenantId,
                        schemeCode
                );


        scheme.archive();


        return repository.save(
                scheme
        );

    }



    public GradingScheme activate(
            UUID tenantId,
            String schemeCode
    ) {

        GradingScheme scheme =
                findByCode(
                        tenantId,
                        schemeCode
                );


        scheme.activate();


        return repository.save(
                scheme
        );

    }

}
