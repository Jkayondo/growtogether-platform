package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class CampusService {


    private final CampusRepository repository;
    private final SchoolProfileService schoolProfiles;


    public CampusService(
            CampusRepository repository,
            SchoolProfileService schoolProfiles
    ) {
        this.repository = repository;
        this.schoolProfiles = schoolProfiles;
    }



    public Campus create(
            UUID tenantId,
            UUID schoolProfileId,
            String campusCode,
            String campusName,
            String addressLine,
            String district,
            String city,
            String countryCode,
            String phoneNumber,
            String email,
            boolean mainCampus
    ) {

        schoolProfiles.get(
                tenantId,
                schoolProfileId
        );


        Campus campus =
                new Campus(
                        schoolProfileId,
                        campusCode,
                        campusName,
                        addressLine,
                        district,
                        city,
                        countryCode,
                        phoneNumber,
                        email,
                        mainCampus
                );


        campus.setTenantId(tenantId);


        return repository.save(campus);

    }



    @Transactional(readOnly = true)
    public List<Campus> findBySchoolProfile(
            UUID tenantId,
            UUID schoolProfileId
    ) {

        return repository
                .findByTenantIdAndSchoolProfileId(
                        tenantId,
                        schoolProfileId
                );

    }



    @Transactional(readOnly = true)
    public Campus findByCode(
            UUID tenantId,
            String campusCode
    ) {

        return repository
                .findByTenantIdAndCampusCode(
                        tenantId,
                        campusCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found."
                        )
                );

    }



    public Campus activate(
            Campus campus
    ) {

        campus.activate();

        return repository.save(campus);

    }



    public Campus deactivate(
            Campus campus
    ) {

        campus.deactivate();

        return repository.save(campus);

    }

}
