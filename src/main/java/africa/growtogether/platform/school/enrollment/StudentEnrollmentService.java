package africa.growtogether.platform.school.enrollment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.student.StudentRepository;

import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class StudentEnrollmentService {

    private static final Set<String> ALLOWED_ENROLLMENT_TYPES =
            Set.of(
                    "NEW",
                    "CONTINUING",
                    "REPEAT",
                    "TRANSFER_IN",
                    "REINSTATEMENT",
                    "PROMOTION"
            );

    private final StudentEnrollmentRepository repository;
    private final StudentRepository students;
    private final AcademicYearRepository academicYears;
    private final AcademicTermRepository academicTerms;
    private final CampusRepository campuses;
    private final ClassGradeRepository classGrades;
    private final StreamRepository streams;

    public StudentEnrollmentService(
            StudentEnrollmentRepository repository,
            StudentRepository students,
            AcademicYearRepository academicYears,
            AcademicTermRepository academicTerms,
            CampusRepository campuses,
            ClassGradeRepository classGrades,
            StreamRepository streams
    ) {

        this.repository = repository;
        this.students = students;
        this.academicYears = academicYears;
        this.academicTerms = academicTerms;
        this.campuses = campuses;
        this.classGrades = classGrades;
        this.streams = streams;
    }

    @Transactional
    public StudentEnrollment create(
            UUID tenantId,
            UUID studentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID classGradeId,
            UUID streamId,
            String enrollmentNumber,
            LocalDate enrollmentDate,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String enrollmentType,
            UUID previousEnrollmentId,
            UUID workflowInstanceId,
            UUID enrolledBy
    ) {

        if (
                enrollmentNumber == null
                || enrollmentNumber.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "enrollmentNumber must not be blank"
            );
        }

        if (enrollmentDate == null) {
            throw new IllegalArgumentException(
                    "enrollmentDate must not be null"
            );
        }

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        String normalizedType =
                enrollmentType == null
                        || enrollmentType.isBlank()
                        ? "NEW"
                        : enrollmentType.trim();

        if (
                !ALLOWED_ENROLLMENT_TYPES.contains(
                        normalizedType
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid enrollment type: "
                            + normalizedType
            );
        }

        students
                .findByTenantIdAndId(
                        tenantId,
                        studentId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student not found for tenant"
                        )
                );

        academicYears
                .findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Academic year not found for tenant"
                        )
                );

        if (academicTermId != null) {

            AcademicTerm term =
                    academicTerms
                            .findByTenantIdAndId(
                                    tenantId,
                                    academicTermId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Academic term not found for tenant"
                                    )
                            );

            if (
                    term.getAcademicYear() == null
                    || !academicYearId.equals(
                            term.getAcademicYear().getId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Academic term does not belong to academic year"
                );
            }
        }

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        campusId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        classGrades
                .findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found for tenant"
                        )
                );

        if (streamId != null) {

            Stream stream =
                    streams
                            .findByTenantIdAndId(
                                    tenantId,
                                    streamId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Stream not found for tenant"
                                    )
                            );

            if (
                    !campusId.equals(
                            stream.getCampusId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Stream does not belong to campus"
                );
            }

            if (
                    !classGradeId.equals(
                            stream.getClassGradeId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Stream does not belong to class grade"
                );
            }
        }

        if (
                repository.existsByTenantIdAndEnrollmentNumber(
                        tenantId,
                        enrollmentNumber.trim()
                )
        ) {
            throw new IllegalArgumentException(
                    "Enrollment number already exists for tenant"
            );
        }

        /*
         * This application-level check also protects the case where
         * academicTermId is null. PostgreSQL ordinary UNIQUE
         * constraints allow multiple NULL values.
         */
        if (
                repository
                        .existsByTenantIdAndStudentIdAndAcademicYearIdAndAcademicTermId(
                                tenantId,
                                studentId,
                                academicYearId,
                                academicTermId
                        )
        ) {
            throw new IllegalArgumentException(
                    "Student already has enrollment for academic period"
            );
        }

        /*
         * V034 permits only one ACTIVE enrollment record for a
         * learner at a time. Check explicitly before persistence so
         * callers receive a clear domain error rather than only a
         * database constraint violation.
         */
        if (
                repository
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                tenantId,
                                studentId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "Student already has an active enrollment"
            );
        }

        if (previousEnrollmentId != null) {

            StudentEnrollment previous =
                    repository
                            .findByTenantIdAndId(
                                    tenantId,
                                    previousEnrollmentId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Previous enrollment not found for tenant"
                                    )
                            );

            if (
                    !studentId.equals(
                            previous.getStudentId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Previous enrollment belongs to another student"
                );
            }
        }

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        studentId,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        enrollmentNumber.trim(),
                        enrollmentDate,
                        effectiveFrom,
                        effectiveTo,
                        normalizedType,
                        previousEnrollmentId,
                        workflowInstanceId,
                        enrolledBy
                );

        enrollment.setTenantId(
                tenantId
        );

        return repository.save(
                enrollment
        );
    }

    @Transactional(readOnly = true)
    public StudentEnrollment findById(
            UUID tenantId,
            UUID enrollmentId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        enrollmentId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student enrollment not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public StudentEnrollment findByEnrollmentNumber(
            UUID tenantId,
            String enrollmentNumber
    ) {

        return repository
                .findByTenantIdAndEnrollmentNumber(
                        tenantId,
                        enrollmentNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student enrollment not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<StudentEnrollment> findByStudent(
            UUID tenantId,
            UUID studentId
    ) {

        return repository
                .findByTenantIdAndStudentIdOrderByEnrollmentDateDesc(
                        tenantId,
                        studentId
                );
    }

    @Transactional(readOnly = true)
    public List<StudentEnrollment> findByAcademicYear(
            UUID tenantId,
            UUID academicYearId
    ) {

        return repository
                .findByTenantIdAndAcademicYearId(
                        tenantId,
                        academicYearId
                );
    }

    @Transactional(readOnly = true)
    public List<StudentEnrollment> findByCampus(
            UUID tenantId,
            UUID campusId
    ) {

        return repository
                .findByTenantIdAndCampusId(
                        tenantId,
                        campusId
                );
    }

    @Transactional(readOnly = true)
    public List<StudentEnrollment> findByClassGrade(
            UUID tenantId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndClassGradeId(
                        tenantId,
                        classGradeId
                );
    }

    @Transactional(readOnly = true)
    public List<StudentEnrollment> findByStream(
            UUID tenantId,
            UUID streamId
    ) {

        return repository
                .findByTenantIdAndStreamId(
                        tenantId,
                        streamId
                );
    }

    @Transactional(readOnly = true)
    public List<StudentEnrollment> findByStatus(
            UUID tenantId,
            String enrollmentStatus
    ) {

        return repository
                .findByTenantIdAndEnrollmentStatus(
                        tenantId,
                        enrollmentStatus
                );
    }

    @Transactional
    public StudentEnrollment markPending(
            UUID tenantId,
            UUID enrollmentId
    ) {

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        enrollment.markPending();

        return repository.save(
                enrollment
        );
    }

    @Transactional
    public StudentEnrollment activate(
            UUID tenantId,
            UUID enrollmentId,
            UUID approvedBy
    ) {

        if (approvedBy == null) {
            throw new IllegalArgumentException(
                    "approvedBy must not be null"
            );
        }

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        repository
                .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                        tenantId,
                        enrollment.getStudentId(),
                        "ACTIVE",
                        EntityStatus.ACTIVE
                )
                .filter(
                        current ->
                                !current.getId().equals(
                                        enrollment.getId()
                                )
                )
                .ifPresent(
                        current -> {
                            throw new IllegalArgumentException(
                                    "Student already has another active enrollment"
                            );
                        }
                );

        enrollment.activate(
                approvedBy
        );

        return repository.save(
                enrollment
        );
    }

    @Transactional
    public StudentEnrollment suspend(
            UUID tenantId,
            UUID enrollmentId
    ) {

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        enrollment.suspend();

        return repository.save(
                enrollment
        );
    }

    @Transactional
    public StudentEnrollment complete(
            UUID tenantId,
            UUID enrollmentId,
            LocalDate effectiveTo
    ) {

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        enrollment.complete(
                effectiveTo
        );

        return repository.save(
                enrollment
        );
    }

    @Transactional
    public StudentEnrollment withdraw(
            UUID tenantId,
            UUID enrollmentId,
            LocalDate exitDate,
            String exitReason
    ) {

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        enrollment.withdraw(
                exitDate,
                exitReason
        );

        return repository.save(
                enrollment
        );
    }

    @Transactional
    public StudentEnrollment transfer(
            UUID tenantId,
            UUID enrollmentId,
            LocalDate exitDate,
            String exitReason
    ) {

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        enrollment.transfer(
                exitDate,
                exitReason
        );

        return repository.save(
                enrollment
        );
    }

    @Transactional
    public StudentEnrollment cancel(
            UUID tenantId,
            UUID enrollmentId
    ) {

        StudentEnrollment enrollment =
                findById(
                        tenantId,
                        enrollmentId
                );

        enrollment.cancel();

        return repository.save(
                enrollment
        );
    }
}
