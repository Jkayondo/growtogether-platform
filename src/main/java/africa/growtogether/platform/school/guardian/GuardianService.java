package africa.growtogether.platform.school.guardian;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardianService {

    private final GuardianRepository repository;

    public GuardianService(
            GuardianRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public Guardian create(
            CreateGuardianCommand command
    ) {

        if (repository.existsByGuardianNumber(
                command.guardianNumber()
        )) {
            throw new IllegalArgumentException(
                    "Guardian number already exists"
            );
        }


        Guardian guardian =
                new Guardian(
                        command.guardianNumber(),
                        command.firstName(),
                        command.middleName(),
                        command.lastName(),
                        command.preferredName(),
                        command.dateOfBirth(),
                        command.gender(),
                        command.nationalityCode(),
                        command.nationalIdNumber(),
                        command.passportNumber(),
                        command.primaryPhoneNumber(),
                        command.alternativePhoneNumber(),
                        command.email(),
                        command.physicalAddress(),
                        command.postalAddress(),
                        command.occupation(),
                        command.employer(),
                        command.eiamUserId(),
                        command.sourceAdmissionGuardianId(),
                        command.preferredLanguage()
                );


        return repository.save(guardian);
    }


    @Transactional(readOnly = true)
    public Guardian get(UUID id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Guardian not found"
                        )
                );
    }
}
