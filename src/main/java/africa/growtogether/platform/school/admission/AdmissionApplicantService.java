package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AdmissionApplicantService {

    private final AdmissionApplicantRepository repository;
    private final AdmissionApplicationRepository applications;
    private final SchoolProfileService schoolProfiles;

    public AdmissionApplicantService(
            AdmissionApplicantRepository repository,
            AdmissionApplicationRepository applications,
            SchoolProfileService schoolProfiles
    ) {
        this.repository = repository;
        this.applications = applications;
        this.schoolProfiles = schoolProfiles;
    }

    @Transactional
    public AdmissionApplicant create(
            UUID tenantId,
            UUID admissionApplicationId,
            CreateAdmissionApplicantCommand command
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

        /*
         * Applicant identity is part of the editable admission form.
         * Once the application leaves DRAFT, later lifecycle services
         * must control any permitted correction through governed actions.
         */
        if (
                !"DRAFT".equals(
                        application.getAdmissionStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Applicant cannot be added after admission application leaves DRAFT"
            );
        }

        if (
                repository.existsByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        admissionApplicationId
                )
        ) {
            throw new IllegalStateException(
                    "Admission applicant already exists for application"
            );
        }

        if (command.dateOfBirth() == null) {
            throw new IllegalArgumentException(
                    "dateOfBirth must not be null"
            );
        }

        ZoneId schoolTimezone =
                schoolProfiles.requireTimezone(
                        tenantId
                );

        LocalDate today =
                LocalDate.now(
                        schoolTimezone
                );

        if (
                command.dateOfBirth().isAfter(
                        today
                )
        ) {
            throw new IllegalArgumentException(
                    "dateOfBirth must not be in the future"
            );
        }

        AdmissionApplicant applicant =
                new AdmissionApplicant(
                        admissionApplicationId,
                        command.firstName(),
                        command.middleName(),
                        command.lastName(),
                        command.preferredName(),
                        command.dateOfBirth(),
                        command.gender(),
                        command.nationalityCode(),
                        command.countryOfBirthCode(),
                        command.primaryLanguage(),
                        command.religion(),
                        command.nationalIdNumber(),
                        command.passportNumber(),
                        command.birthCertificateNumber(),
                        command.email(),
                        command.phoneNumber(),
                        command.physicalAddress(),
                        command.existingEiamUserId(),
                        command.existingLearnerReference()
                );

        applicant.setTenantId(
                tenantId
        );

        return repository.save(
                applicant
        );
    }

    @Transactional(readOnly = true)
    public AdmissionApplicant get(
            UUID tenantId,
            UUID applicantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (applicantId == null) {
            throw new IllegalArgumentException(
                    "applicantId must not be null"
            );
        }

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        applicantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission applicant not found for tenant"
                        )
                );
    }

    @Transactional(readOnly = true)
    public AdmissionApplicant getForApplication(
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

        return repository
                .findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        admissionApplicationId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission applicant not found for application"
                        )
                );
    }
}
