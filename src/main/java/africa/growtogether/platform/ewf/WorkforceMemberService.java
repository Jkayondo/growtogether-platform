package africa.growtogether.platform.ewf;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WorkforceMemberService {

    private final WorkforceMemberRepository repository;

    public WorkforceMemberService(
            WorkforceMemberRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public WorkforceMember create(
            UUID tenantId,
            String workforceNumber,
            String employeeNumber,
            String firstName,
            String middleName,
            String lastName,
            String preferredName,
            LocalDate dateOfBirth,
            String gender,
            String nationalityCode,
            String nationalIdNumber,
            String passportNumber,
            String primaryPhoneNumber,
            String alternativePhoneNumber,
            String email,
            String physicalAddress,
            String postalAddress,
            UUID eiamUserId,
            UUID edsPersonnelFileId,
            String workforceCategory
    ) {

        WorkforceMember member =
                new WorkforceMember(
                        workforceNumber,
                        employeeNumber,
                        firstName,
                        middleName,
                        lastName,
                        preferredName,
                        dateOfBirth,
                        gender,
                        nationalityCode,
                        nationalIdNumber,
                        passportNumber,
                        primaryPhoneNumber,
                        alternativePhoneNumber,
                        email,
                        physicalAddress,
                        postalAddress,
                        eiamUserId,
                        edsPersonnelFileId,
                        workforceCategory
                );

        member.setTenantId(
                tenantId
        );

        return repository.save(
                member
        );
    }

    @Transactional(readOnly = true)
    public WorkforceMember findByWorkforceNumber(
            UUID tenantId,
            String workforceNumber
    ) {

        return repository
                .findByTenantIdAndWorkforceNumber(
                        tenantId,
                        workforceNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Workforce member not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public WorkforceMember findByEmployeeNumber(
            UUID tenantId,
            String employeeNumber
    ) {

        return repository
                .findByTenantIdAndEmployeeNumber(
                        tenantId,
                        employeeNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Workforce member not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<WorkforceMember> findByEmail(
            UUID tenantId,
            String email
    ) {

        return repository
                .findByTenantIdAndEmail(
                        tenantId,
                        email
                );
    }

    @Transactional(readOnly = true)
    public List<WorkforceMember> findByCategory(
            UUID tenantId,
            String workforceCategory
    ) {

        return repository
                .findByTenantIdAndWorkforceCategory(
                        tenantId,
                        workforceCategory
                );
    }

    @Transactional(readOnly = true)
    public List<WorkforceMember> findByStatus(
            UUID tenantId,
            String workforceStatus
    ) {

        return repository
                .findByTenantIdAndWorkforceStatus(
                        tenantId,
                        workforceStatus
                );
    }
}
