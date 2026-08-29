package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdmissionGuardianService {

    private final AdmissionGuardianRepository repository;
    private final AdmissionApplicationRepository applications;

    public AdmissionGuardianService(
            AdmissionGuardianRepository repository,
            AdmissionApplicationRepository applications
    ) {
        this.repository = repository;
        this.applications = applications;
    }

    @Transactional
    public AdmissionGuardian create(
            UUID tenantId,
            UUID admissionApplicationId,
            CreateAdmissionGuardianCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (admissionApplicationId == null) {
            throw new IllegalArgumentException(
                    "admissionApplicationId must not be null"
            );
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "command must not be null"
            );
        }

        AdmissionApplication application =
                applications
                        .findByTenantIdAndId(
                                tenantId,
                                admissionApplicationId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Admission application not found for tenant"
                                )
                        );

        if (
                !"DRAFT".equals(
                        application.getAdmissionStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Guardian cannot be added after admission application leaves DRAFT"
            );
        }

        if (
                Boolean.TRUE.equals(
                        command.primaryGuardian()
                )
                && repository
                        .existsByTenantIdAndAdmissionApplicationIdAndPrimaryGuardianTrueAndStatus(
                                tenantId,
                                admissionApplicationId,
                                EntityStatus.ACTIVE
                        )
        ) {
            throw new IllegalStateException(
                    "Active primary guardian already exists for application"
            );
        }

        AdmissionGuardian guardian =
                new AdmissionGuardian(
                        admissionApplicationId,
                        command.relationshipType(),
                        command.firstName(),
                        command.middleName(),
                        command.lastName(),
                        command.phoneNumber(),
                        command.alternativePhoneNumber(),
                        command.email(),
                        command.occupation(),
                        command.employer(),
                        command.physicalAddress(),
                        command.nationalIdNumber(),
                        command.existingEiamUserId(),
                        command.primaryGuardian(),
                        command.emergencyContact(),
                        command.authorizedToCollect(),
                        command.receivesCommunications(),
                        command.financialResponsibility()
                );

        guardian.setTenantId(
                tenantId
        );

        return repository.save(
                guardian
        );
    }

    @Transactional(readOnly = true)
    public AdmissionGuardian get(
            UUID tenantId,
            UUID guardianId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (guardianId == null) {
            throw new IllegalArgumentException(
                    "guardianId must not be null"
            );
        }

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission guardian not found for tenant"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<AdmissionGuardian> listForApplication(
            UUID tenantId,
            UUID admissionApplicationId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (admissionApplicationId == null) {
            throw new IllegalArgumentException(
                    "admissionApplicationId must not be null"
            );
        }

        applications
                .findByTenantIdAndId(
                        tenantId,
                        admissionApplicationId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission application not found for tenant"
                        )
                );

        return repository
                .findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        admissionApplicationId
                );
    }
}
