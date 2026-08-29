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
            UUID tenantId,
            CreateGuardianCommand command
    ) {

        if (
                repository.existsByTenantIdAndGuardianNumber(
                        tenantId,
                        command.guardianNumber()
                )
        ) {
            throw new IllegalArgumentException(
                    "Guardian number already exists for tenant"
            );
        }

        if (
                command.sourceAdmissionGuardianId() != null
                && repository
                        .existsByTenantIdAndSourceAdmissionGuardianId(
                                tenantId,
                                command.sourceAdmissionGuardianId()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Admission guardian already has a guardian profile"
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

        guardian.setTenantId(
                tenantId
        );

        return repository.save(
                guardian
        );
    }

    @Transactional(readOnly = true)
    public Guardian get(
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
                                "Guardian not found"
                        )
                );
    }

    @Transactional
    public Guardian markVerificationPending(
            UUID tenantId,
            UUID guardianId
    ) {

        Guardian guardian =
                get(
                        tenantId,
                        guardianId
                );

        guardian.markVerificationPending();

        return repository.save(
                guardian
        );
    }

    @Transactional
    public Guardian verify(
            UUID tenantId,
            UUID guardianId,
            UUID verifiedBy
    ) {

        Guardian guardian =
                get(
                        tenantId,
                        guardianId
                );

        guardian.verify(
                verifiedBy
        );

        return repository.save(
                guardian
        );
    }

    @Transactional
    public Guardian restrict(
            UUID tenantId,
            UUID guardianId
    ) {

        Guardian guardian =
                get(
                        tenantId,
                        guardianId
                );

        guardian.restrict();

        return repository.save(
                guardian
        );
    }

    @Transactional
    public Guardian activate(
            UUID tenantId,
            UUID guardianId
    ) {

        Guardian guardian =
                get(
                        tenantId,
                        guardianId
                );

        guardian.activate();

        return repository.save(
                guardian
        );
    }

    @Transactional
    public Guardian deactivate(
            UUID tenantId,
            UUID guardianId
    ) {

        Guardian guardian =
                get(
                        tenantId,
                        guardianId
                );

        guardian.deactivate();

        return repository.save(
                guardian
        );
    }
}
