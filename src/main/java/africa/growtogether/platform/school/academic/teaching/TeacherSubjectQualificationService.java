package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherSubjectQualificationService {

    private final TeacherSubjectQualificationRepository repository;
    private final TeacherProfileRepository teacherProfiles;
    private final SubjectRepository subjects;
    private final ClassGradeRepository classGrades;

    public TeacherSubjectQualificationService(
            TeacherSubjectQualificationRepository repository,
            TeacherProfileRepository teacherProfiles,
            SubjectRepository subjects,
            ClassGradeRepository classGrades
    ) {
        this.repository = repository;
        this.teacherProfiles = teacherProfiles;
        this.subjects = subjects;
        this.classGrades = classGrades;
    }

    @Transactional
    public TeacherSubjectQualification create(
            UUID tenantId,
            UUID teacherProfileId,
            UUID subjectId,
            String competencyLevel,
            boolean primarySubject,
            UUID minimumClassGradeId,
            UUID maximumClassGradeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID edsEvidenceDocumentId
    ) {

        teacherProfiles
                .findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher profile not found for tenant"
                        )
                );

        subjects
                .findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject not found for tenant"
                        )
                );

        if (minimumClassGradeId != null) {
            classGrades
                    .findByTenantIdAndId(
                            tenantId,
                            minimumClassGradeId
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Minimum class grade not found for tenant"
                            )
                    );
        }

        if (maximumClassGradeId != null) {
            classGrades
                    .findByTenantIdAndId(
                            tenantId,
                            maximumClassGradeId
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Maximum class grade not found for tenant"
                            )
                    );
        }

        repository
                .findByTenantIdAndTeacherProfileIdAndSubjectId(
                        tenantId,
                        teacherProfileId,
                        subjectId
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Teacher already has a qualification for this subject"
                    );
                });

        if (primarySubject) {
            repository
                    .findByTenantIdAndTeacherProfileIdAndPrimarySubjectTrueAndStatus(
                            tenantId,
                            teacherProfileId,
                            EntityStatus.ACTIVE
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Teacher already has an active primary subject"
                        );
                    });
        }

        LocalDate resolvedEffectiveFrom =
                effectiveFrom != null
                        ? effectiveFrom
                        : LocalDate.now();

        if (effectiveTo != null
                && effectiveTo.isBefore(resolvedEffectiveFrom)) {

            throw new IllegalArgumentException(
                    "Qualification effective-to date cannot be before effective-from date"
            );
        }

        TeacherSubjectQualification qualification =
                new TeacherSubjectQualification(
                        teacherProfileId,
                        subjectId,
                        competencyLevel,
                        primarySubject,
                        minimumClassGradeId,
                        maximumClassGradeId,
                        resolvedEffectiveFrom,
                        effectiveTo,
                        edsEvidenceDocumentId
                );

        qualification.setTenantId(
                tenantId
        );

        return repository.save(
                qualification
        );
    }

    @Transactional(readOnly = true)
    public TeacherSubjectQualification findById(
            UUID tenantId,
            UUID qualificationId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        qualificationId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher subject qualification not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<TeacherSubjectQualification> findByTeacher(
            UUID tenantId,
            UUID teacherProfileId
    ) {

        return repository
                .findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                );
    }

    @Transactional(readOnly = true)
    public List<TeacherSubjectQualification> findBySubject(
            UUID tenantId,
            UUID subjectId
    ) {

        return repository
                .findByTenantIdAndSubjectId(
                        tenantId,
                        subjectId
                );
    }

    @Transactional(readOnly = true)
    public List<TeacherSubjectQualification> findByVerificationStatus(
            UUID tenantId,
            String verificationStatus
    ) {

        return repository
                .findByTenantIdAndVerificationStatus(
                        tenantId,
                        verificationStatus
                );
    }

    @Transactional
    public TeacherSubjectQualification verify(
            UUID tenantId,
            UUID qualificationId,
            UUID verifiedBy
    ) {

        TeacherSubjectQualification qualification =
                findById(
                        tenantId,
                        qualificationId
                );

        qualification.verify(
                verifiedBy
        );

        return repository.save(
                qualification
        );
    }

    @Transactional
    public TeacherSubjectQualification reject(
            UUID tenantId,
            UUID qualificationId
    ) {

        TeacherSubjectQualification qualification =
                findById(
                        tenantId,
                        qualificationId
                );

        qualification.reject();

        return repository.save(
                qualification
        );
    }
}
