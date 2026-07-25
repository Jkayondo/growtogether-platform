package africa.growtogether.platform.school.profile;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolProfileService {

    private final SchoolProfileRepository repository;

    public SchoolProfileService(
            SchoolProfileRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public SchoolProfile create(
            CreateSchoolProfileCommand command
    ) {

        SchoolProfile schoolProfile =
                new SchoolProfile(
                        command.schoolCode(),
                        command.schoolName(),
                        command.legalName(),
                        command.educationSystem(),
                        command.countryCode(),
                        command.defaultCurrency(),
                        command.timezone(),
                        command.email(),
                        command.phoneNumber(),
                        command.website()
                );

        return repository.save(schoolProfile);
    }

    @Transactional(readOnly = true)
    public SchoolProfile get(UUID id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "School profile not found"
                        )
                );
    }
}
