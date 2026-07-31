package africa.growtogether.platform.school.configuration;


import africa.growtogether.platform.school.configuration.dto.CreateSchoolConfigurationRequest;
import africa.growtogether.platform.school.configuration.dto.SchoolConfigurationResponse;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class SchoolConfigurationService {


    private final SchoolConfigurationRepository repository;


    public SchoolConfigurationService(
            SchoolConfigurationRepository repository
    ) {

        this.repository = repository;
    }


    public SchoolConfigurationResponse create(
            UUID tenantId,
            CreateSchoolConfigurationRequest request
    ) {


        SchoolConfiguration configuration =
                new SchoolConfiguration(
                        tenantId,
                        request.schoolName(),
                        request.countryCode(),
                        request.schoolType()
                );


        SchoolConfiguration saved =
                repository.save(configuration);


        return new SchoolConfigurationResponse(
                saved.getId(),
                saved.getSchoolName(),
                saved.getCountryCode(),
                saved.getSchoolType()
        );
    }
}
