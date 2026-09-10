package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class EducationGradingProfileService {


    private final EducationGradingProfileRepository repository;


    public EducationGradingProfileService(
            EducationGradingProfileRepository repository
    ) {

        this.repository = repository;

    }



    public EducationGradingProfile create(
            UUID tenantId,
            EducationGradingProfile profile
    ) {


        if (tenantId == null) {

            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );

        }


        if (
                profile.getProfileCode() == null
                || profile.getProfileCode().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Profile code is required"
            );

        }


        if (
                repository.existsByTenantIdAndProfileCode(
                        tenantId,
                        profile.getProfileCode()
                )
        ) {

            throw new IllegalArgumentException(
                    "Education grading profile already exists"
            );

        }


        profile.setTenantId(
                tenantId
        );


        return repository.save(
                profile
        );

    }



    @Transactional(readOnly = true)
    public EducationGradingProfile findByCode(
            UUID tenantId,
            String profileCode
    ) {

        return repository
                .findByTenantIdAndProfileCode(
                        tenantId,
                        profileCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Education grading profile not found"
                        )
                );

    }



    @Transactional(readOnly = true)
    public List<EducationGradingProfile> findActive(
            UUID tenantId
    ) {

        return repository
                .findByTenantIdAndStatus(
                        tenantId,
                        "ACTIVE"
                );

    }



    @Transactional(readOnly = true)
    public List<EducationGradingProfile> findByCountry(
            UUID tenantId,
            String countryCode
    ) {

        return repository
                .findByTenantIdAndCountryCode(
                        tenantId,
                        countryCode
                );

    }



    public EducationGradingProfile archive(
            UUID tenantId,
            String profileCode
    ) {

        EducationGradingProfile profile =
                findByCode(
                        tenantId,
                        profileCode
                );


        profile.archive();


        return repository.save(
                profile
        );

    }



    public EducationGradingProfile activate(
            UUID tenantId,
            String profileCode
    ) {

        EducationGradingProfile profile =
                findByCode(
                        tenantId,
                        profileCode
                );


        profile.activate();


        return repository.save(
                profile
        );

    }

}
