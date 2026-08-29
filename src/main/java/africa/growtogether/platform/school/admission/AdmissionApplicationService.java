package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AdmissionApplicationService {

    private final AdmissionApplicationRepository repository;
    private final AdmissionNumberService admissionNumbers;
    private final AcademicYearRepository academicYears;
    private final CampusRepository campuses;
    private final ClassGradeRepository classGrades;
    private final StreamRepository streams;
    private final SchoolProfileService schoolProfiles;

    public AdmissionApplicationService(
            AdmissionApplicationRepository repository,
            AdmissionNumberService admissionNumbers,
            AcademicYearRepository academicYears,
            CampusRepository campuses,
            ClassGradeRepository classGrades,
            StreamRepository streams,
            SchoolProfileService schoolProfiles
    ) {
        this.repository = repository;
        this.admissionNumbers = admissionNumbers;
        this.academicYears = academicYears;
        this.campuses = campuses;
        this.classGrades = classGrades;
        this.streams = streams;
        this.schoolProfiles = schoolProfiles;
    }

    @Transactional
    public AdmissionApplication createDraft(
            UUID tenantId,
            CreateAdmissionApplicationCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "command must not be null"
            );
        }

        academicYears
                .findByTenantIdAndId(
                        tenantId,
                        command.academicYearId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Academic year not found for tenant"
                        )
                );

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        command.campusId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        classGrades
                .findByTenantIdAndId(
                        tenantId,
                        command.desiredClassGradeId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found for tenant"
                        )
                );

        if (command.desiredStreamId() != null) {

            Stream stream =
                    streams
                            .findByTenantIdAndId(
                                    tenantId,
                                    command.desiredStreamId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Stream not found for tenant"
                                    )
                            );

            if (
                    !command.campusId().equals(
                            stream.getCampusId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Stream does not belong to campus"
                );
            }

            if (
                    !command.desiredClassGradeId().equals(
                            stream.getClassGradeId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Stream does not belong to class grade"
                );
            }
        }

        ZoneId schoolTimezone =
                schoolProfiles.requireTimezone(
                        tenantId
                );

        LocalDate applicationDate =
                LocalDate.now(
                        schoolTimezone
                );

        String applicationNumber =
                admissionNumbers.next(
                        tenantId,
                        command.academicYearId()
                );

        AdmissionApplication application =
                new AdmissionApplication(
                        applicationNumber,
                        command.academicYearId(),
                        command.campusId(),
                        command.desiredClassGradeId(),
                        command.desiredStreamId(),
                        applicationDate,
                        command.submissionChannel()
                );

        application.setTenantId(
                tenantId
        );

        return repository.save(
                application
        );
    }

    @Transactional(readOnly = true)
    public AdmissionApplication get(
            UUID tenantId,
            UUID applicationId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission application not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public AdmissionApplication getByApplicationNumber(
            UUID tenantId,
            String applicationNumber
    ) {

        if (
                applicationNumber == null
                || applicationNumber.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "applicationNumber must not be blank"
            );
        }

        return repository
                .findByTenantIdAndApplicationNumber(
                        tenantId,
                        applicationNumber.trim()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Admission application not found"
                        )
                );
    }
}
