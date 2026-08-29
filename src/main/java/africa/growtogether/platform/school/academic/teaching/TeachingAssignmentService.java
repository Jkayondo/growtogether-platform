package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TeachingAssignmentService {

    private static final Set<String> ALLOWED_ASSIGNMENT_TYPES =
            Set.of(
                    "PRIMARY_TEACHER",
                    "ASSISTANT_TEACHER",
                    "RELIEF_TEACHER",
                    "PRACTICAL_INSTRUCTOR",
                    "SPECIAL_NEEDS_SUPPORT",
                    "REMOTE_TEACHER",
                    "OTHER"
            );

    private final TeachingAssignmentRepository repository;
    private final TeacherProfileRepository teachers;
    private final AcademicYearRepository academicYears;
    private final AcademicTermRepository academicTerms;
    private final CampusRepository campuses;
    private final ClassGradeRepository classGrades;
    private final StreamRepository streams;
    private final SubjectRepository subjects;

    public TeachingAssignmentService(
            TeachingAssignmentRepository repository,
            TeacherProfileRepository teachers,
            AcademicYearRepository academicYears,
            AcademicTermRepository academicTerms,
            CampusRepository campuses,
            ClassGradeRepository classGrades,
            StreamRepository streams,
            SubjectRepository subjects
    ) {
        this.repository = repository;
        this.teachers = teachers;
        this.academicYears = academicYears;
        this.academicTerms = academicTerms;
        this.campuses = campuses;
        this.classGrades = classGrades;
        this.streams = streams;
        this.subjects = subjects;
    }

    @Transactional
    public TeachingAssignment create(
            UUID tenantId,
            String assignmentReference,
            UUID teacherProfileId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID classGradeId,
            UUID streamId,
            UUID subjectId,
            String assignmentType,
            int weeklyPeriods,
            BigDecimal workloadPercentage,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String roomReference
    ) {

        if (assignmentReference == null || assignmentReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Assignment reference is required"
            );
        }

        repository
                .findByTenantIdAndAssignmentReference(
                        tenantId,
                        assignmentReference
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Teaching assignment reference already exists"
                    );
                });

        teachers
                .findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher profile not found for tenant"
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

            if (!term.getAcademicYear()
                    .getId()
                    .equals(academicYearId)) {

                throw new IllegalArgumentException(
                        "Academic term does not belong to the selected academic year"
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

            if (!stream.getCampusId().equals(campusId)) {
                throw new IllegalArgumentException(
                        "Stream does not belong to the selected campus"
                );
            }

            if (!stream.getClassGradeId().equals(classGradeId)) {
                throw new IllegalArgumentException(
                        "Stream does not belong to the selected class grade"
                );
            }
        }

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

        if (weeklyPeriods <= 0) {
            throw new IllegalArgumentException(
                    "Weekly periods must be greater than zero"
            );
        }

        if (workloadPercentage != null) {

            if (workloadPercentage.compareTo(BigDecimal.ZERO) <= 0
                    || workloadPercentage.compareTo(
                    new BigDecimal("100")
            ) > 0) {

                throw new IllegalArgumentException(
                        "Workload percentage must be greater than zero and not exceed 100"
                );
            }
        }

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "Effective-from date is required"
            );
        }

        if (effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)) {

            throw new IllegalArgumentException(
                    "Effective-to date cannot be before effective-from date"
            );
        }

        String resolvedAssignmentType =
                assignmentType == null || assignmentType.isBlank()
                        ? "PRIMARY_TEACHER"
                        : assignmentType;

        if (!ALLOWED_ASSIGNMENT_TYPES.contains(
                resolvedAssignmentType
        )) {
            throw new IllegalArgumentException(
                    "Invalid teaching assignment type"
            );
        }

        boolean duplicateScope =
                repository
                        .existsByTenantIdAndTeacherProfileIdAndAcademicYearIdAndAcademicTermIdAndClassGradeIdAndStreamIdAndSubjectId(
                                tenantId,
                                teacherProfileId,
                                academicYearId,
                                academicTermId,
                                classGradeId,
                                streamId,
                                subjectId
                        );

        if (duplicateScope) {
            throw new IllegalArgumentException(
                    "Teaching assignment already exists for this teacher and academic scope"
            );
        }

        TeachingAssignment assignment =
                new TeachingAssignment(
                        assignmentReference,
                        teacherProfileId,
                        null,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        subjectId,
                        resolvedAssignmentType,
                        weeklyPeriods,
                        workloadPercentage,
                        effectiveFrom,
                        effectiveTo,
                        roomReference,
                        null,
                        null
                );

        assignment.setTenantId(
                tenantId
        );

        return repository.save(
                assignment
        );
    }

    @Transactional(readOnly = true)
    public TeachingAssignment findById(
            UUID tenantId,
            UUID assignmentId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        assignmentId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teaching assignment not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public TeachingAssignment findByReference(
            UUID tenantId,
            String assignmentReference
    ) {

        return repository
                .findByTenantIdAndAssignmentReference(
                        tenantId,
                        assignmentReference
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teaching assignment not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignment> findByTeacher(
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
    public List<TeachingAssignment> findByAcademicYear(
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
    public List<TeachingAssignment> findByCampus(
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
    public List<TeachingAssignment> findByClassGrade(
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
    public List<TeachingAssignment> findBySubject(
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
    public List<TeachingAssignment> findByStatus(
            UUID tenantId,
            String assignmentStatus
    ) {

        return repository
                .findByTenantIdAndAssignmentStatus(
                        tenantId,
                        assignmentStatus
                );
    }

    @Transactional
    public TeachingAssignment requestApproval(
            UUID tenantId,
            UUID assignmentId
    ) {

        TeachingAssignment assignment =
                findById(
                        tenantId,
                        assignmentId
                );

        assignment.markPendingApproval();

        return repository.save(
                assignment
        );
    }

    @Transactional
    public TeachingAssignment activate(
            UUID tenantId,
            UUID assignmentId,
            UUID approvedBy
    ) {

        if (approvedBy == null) {
            throw new IllegalArgumentException(
                    "Approving user is required"
            );
        }

        TeachingAssignment assignment =
                findById(
                        tenantId,
                        assignmentId
                );

        assignment.activate(
                approvedBy
        );

        return repository.save(
                assignment
        );
    }

    @Transactional
    public TeachingAssignment suspend(
            UUID tenantId,
            UUID assignmentId
    ) {

        TeachingAssignment assignment =
                findById(
                        tenantId,
                        assignmentId
                );

        assignment.suspend();

        return repository.save(
                assignment
        );
    }

    @Transactional
    public TeachingAssignment complete(
            UUID tenantId,
            UUID assignmentId
    ) {

        TeachingAssignment assignment =
                findById(
                        tenantId,
                        assignmentId
                );

        assignment.complete();

        return repository.save(
                assignment
        );
    }

    @Transactional
    public TeachingAssignment cancel(
            UUID tenantId,
            UUID assignmentId
    ) {

        TeachingAssignment assignment =
                findById(
                        tenantId,
                        assignmentId
                );

        assignment.cancel();

        return repository.save(
                assignment
        );
    }
}
