package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeachingAssignmentServiceTest {

    @Test
    void createsValidTeachingAssignment() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequired(ids);

        when(
                h.repository.save(
                        any(TeachingAssignment.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        TeachingAssignment result =
                h.service.create(
                        ids.tenantId,
                        "TA-001",
                        ids.teacherProfileId,
                        ids.academicYearId,
                        null,
                        ids.campusId,
                        ids.classGradeId,
                        null,
                        ids.subjectId,
                        null,
                        8,
                        new BigDecimal("40.00"),
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 4, 30),
                        "ROOM-01"
                );

        assertNotNull(result);

        assertEquals(
                "TA-001",
                result.getAssignmentReference()
        );

        assertEquals(
                ids.teacherProfileId,
                result.getTeacherProfileId()
        );

        assertEquals(
                ids.academicYearId,
                result.getAcademicYearId()
        );

        assertEquals(
                ids.campusId,
                result.getCampusId()
        );

        assertEquals(
                ids.classGradeId,
                result.getClassGradeId()
        );

        assertEquals(
                ids.subjectId,
                result.getSubjectId()
        );

        assertEquals(
                "PRIMARY_TEACHER",
                result.getAssignmentType()
        );

        assertEquals(
                "PLANNED",
                result.getAssignmentStatus()
        );

        assertEquals(
                8,
                result.getWeeklyPeriods()
        );

        verify(
                h.repository
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsTeacherProfileNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Teacher profile not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsAcademicYearNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                h.academicYears.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicYearId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Academic year not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsAcademicTermNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                h.academicYears.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                h.academicTerms.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicTermId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.service.create(
                                ids.tenantId,
                                "TA-001",
                                ids.teacherProfileId,
                                ids.academicYearId,
                                ids.academicTermId,
                                ids.campusId,
                                ids.classGradeId,
                                null,
                                ids.subjectId,
                                "PRIMARY_TEACHER",
                                8,
                                null,
                                LocalDate.of(2026, 2, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Academic term not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsAcademicTermBelongingToDifferentAcademicYear() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                h.academicYears.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        AcademicTerm term =
                mock(AcademicTerm.class);

        AcademicYear differentYear =
                mock(AcademicYear.class);

        when(
                differentYear.getId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                term.getAcademicYear()
        ).thenReturn(
                differentYear
        );

        when(
                h.academicTerms.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicTermId
                )
        ).thenReturn(
                Optional.of(term)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.service.create(
                                ids.tenantId,
                                "TA-001",
                                ids.teacherProfileId,
                                ids.academicYearId,
                                ids.academicTermId,
                                ids.campusId,
                                ids.classGradeId,
                                null,
                                ids.subjectId,
                                "PRIMARY_TEACHER",
                                8,
                                null,
                                LocalDate.of(2026, 2, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Academic term does not belong to the selected academic year",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsCampusNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                h.academicYears.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                h.campuses.findByTenantIdAndId(
                        ids.tenantId,
                        ids.campusId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Campus not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsClassGradeNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                h.academicYears.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                h.campuses.findByTenantIdAndId(
                        ids.tenantId,
                        ids.campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                h.classGrades.findByTenantIdAndId(
                        ids.tenantId,
                        ids.classGradeId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Class grade not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsStreamNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequiredBeforeStream(ids);

        when(
                h.streams.findByTenantIdAndId(
                        ids.tenantId,
                        ids.streamId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createWithStream(ids)
                );

        assertEquals(
                "Stream not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsStreamBelongingToDifferentCampus() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequiredBeforeStream(ids);

        Stream stream =
                mock(Stream.class);

        when(
                stream.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                stream.getClassGradeId()
        ).thenReturn(
                ids.classGradeId
        );

        when(
                h.streams.findByTenantIdAndId(
                        ids.tenantId,
                        ids.streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createWithStream(ids)
                );

        assertEquals(
                "Stream does not belong to the selected campus",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsStreamBelongingToDifferentClassGrade() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequiredBeforeStream(ids);

        Stream stream =
                mock(Stream.class);

        when(
                stream.getCampusId()
        ).thenReturn(
                ids.campusId
        );

        when(
                stream.getClassGradeId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                h.streams.findByTenantIdAndId(
                        ids.tenantId,
                        ids.streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createWithStream(ids)
                );

        assertEquals(
                "Stream does not belong to the selected class grade",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsSubjectNotFoundForTenant() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubReferenceAvailable();

        when(
                h.teachers.findByTenantIdAndId(
                        ids.tenantId,
                        ids.teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                h.academicYears.findByTenantIdAndId(
                        ids.tenantId,
                        ids.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                h.campuses.findByTenantIdAndId(
                        ids.tenantId,
                        ids.campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                h.classGrades.findByTenantIdAndId(
                        ids.tenantId,
                        ids.classGradeId
                )
        ).thenReturn(
                Optional.of(
                        mock(ClassGrade.class)
                )
        );

        when(
                h.subjects.findByTenantIdAndId(
                        ids.tenantId,
                        ids.subjectId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Subject not found for tenant",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsDuplicateAssignmentReference() {

        Harness h = new Harness();
        Ids ids = new Ids();

        when(
                h.repository.findByTenantIdAndAssignmentReference(
                        ids.tenantId,
                        "TA-001"
                )
        ).thenReturn(
                Optional.of(
                        mock(TeachingAssignment.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Teaching assignment reference already exists",
                error.getMessage()
        );

        verify(
                h.teachers,
                never()
        ).findByTenantIdAndId(
                any(),
                any()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsDuplicateTeachingScope() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequired(ids);

        when(
                h.repository
                        .existsByTenantIdAndTeacherProfileIdAndAcademicYearIdAndAcademicTermIdAndClassGradeIdAndStreamIdAndSubjectId(
                                ids.tenantId,
                                ids.teacherProfileId,
                                ids.academicYearId,
                                null,
                                ids.classGradeId,
                                null,
                                ids.subjectId
                        )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.createDefault(ids)
                );

        assertEquals(
                "Teaching assignment already exists for this teacher and academic scope",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsNonPositiveWeeklyPeriods() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequired(ids);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.service.create(
                                ids.tenantId,
                                "TA-001",
                                ids.teacherProfileId,
                                ids.academicYearId,
                                null,
                                ids.campusId,
                                ids.classGradeId,
                                null,
                                ids.subjectId,
                                "PRIMARY_TEACHER",
                                0,
                                null,
                                LocalDate.of(2026, 2, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Weekly periods must be greater than zero",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsWorkloadPercentageAboveOneHundred() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequired(ids);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.service.create(
                                ids.tenantId,
                                "TA-001",
                                ids.teacherProfileId,
                                ids.academicYearId,
                                null,
                                ids.campusId,
                                ids.classGradeId,
                                null,
                                ids.subjectId,
                                "PRIMARY_TEACHER",
                                8,
                                new BigDecimal("101.00"),
                                LocalDate.of(2026, 2, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Workload percentage must be greater than zero and not exceed 100",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsEffectiveToBeforeEffectiveFrom() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequired(ids);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.service.create(
                                ids.tenantId,
                                "TA-001",
                                ids.teacherProfileId,
                                ids.academicYearId,
                                null,
                                ids.campusId,
                                ids.classGradeId,
                                null,
                                ids.subjectId,
                                "PRIMARY_TEACHER",
                                8,
                                null,
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 1, 31),
                                null
                        )
                );

        assertEquals(
                "Effective-to date cannot be before effective-from date",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void rejectsInvalidAssignmentType() {

        Harness h = new Harness();
        Ids ids = new Ids();

        h.stubRequired(ids);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> h.service.create(
                                ids.tenantId,
                                "TA-001",
                                ids.teacherProfileId,
                                ids.academicYearId,
                                null,
                                ids.campusId,
                                ids.classGradeId,
                                null,
                                ids.subjectId,
                                "UNAUTHORISED_TYPE",
                                8,
                                null,
                                LocalDate.of(2026, 2, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Invalid teaching assignment type",
                error.getMessage()
        );

        verify(
                h.repository,
                never()
        ).save(
                any(TeachingAssignment.class)
        );
    }

    @Test
    void activateRecordsApprovalAndChangesLifecycleToActive() {

        Harness h = new Harness();
        Ids ids = new Ids();

        UUID assignmentId =
                UUID.randomUUID();

        UUID approvedBy =
                UUID.randomUUID();

        TeachingAssignment assignment =
                new TeachingAssignment(
                        "TA-001",
                        ids.teacherProfileId,
                        null,
                        ids.academicYearId,
                        null,
                        ids.campusId,
                        ids.classGradeId,
                        null,
                        ids.subjectId,
                        "PRIMARY_TEACHER",
                        8,
                        null,
                        LocalDate.of(2026, 2, 1),
                        null,
                        null,
                        null,
                        null
                );

        assignment.setTenantId(
                ids.tenantId
        );

        when(
                h.repository.findByTenantIdAndId(
                        ids.tenantId,
                        assignmentId
                )
        ).thenReturn(
                Optional.of(assignment)
        );

        when(
                h.repository.save(
                        assignment
                )
        ).thenReturn(
                assignment
        );

        TeachingAssignment result =
                h.service.activate(
                        ids.tenantId,
                        assignmentId,
                        approvedBy
                );

        assertEquals(
                "ACTIVE",
                result.getAssignmentStatus()
        );

        assertEquals(
                approvedBy,
                result.getApprovedBy()
        );

        assertNotNull(
                result.getApprovedAt()
        );

        verify(
                h.repository
        ).save(
                assignment
        );
    }

    @Test
    void reactivationPreservesOriginalApprovalEvidence() {

        Harness h = new Harness();
        Ids ids = new Ids();

        UUID assignmentId =
                UUID.randomUUID();

        UUID originalApprovedBy =
                UUID.randomUUID();

        UUID reactivatingUser =
                UUID.randomUUID();

        TeachingAssignment assignment =
                new TeachingAssignment(
                        "TA-REACTIVATE-001",
                        ids.teacherProfileId,
                        null,
                        ids.academicYearId,
                        null,
                        ids.campusId,
                        ids.classGradeId,
                        null,
                        ids.subjectId,
                        "PRIMARY_TEACHER",
                        8,
                        null,
                        LocalDate.of(2026, 2, 1),
                        null,
                        null,
                        null,
                        null
                );

        assignment.setTenantId(
                ids.tenantId
        );

        assignment.activate(
                originalApprovedBy
        );

        var originalApprovedAt =
                assignment.getApprovedAt();

        assignment.suspend();

        when(
                h.repository.findByTenantIdAndId(
                        ids.tenantId,
                        assignmentId
                )
        ).thenReturn(
                java.util.Optional.of(
                        assignment
                )
        );

        when(
                h.repository.save(
                        assignment
                )
        ).thenReturn(
                assignment
        );

        TeachingAssignment result =
                h.service.activate(
                        ids.tenantId,
                        assignmentId,
                        reactivatingUser
                );

        assertEquals(
                "ACTIVE",
                result.getAssignmentStatus()
        );

        assertEquals(
                originalApprovedBy,
                result.getApprovedBy()
        );

        assertEquals(
                originalApprovedAt,
                result.getApprovedAt()
        );

        verify(
                h.repository
        ).save(
                assignment
        );
    }


    private static final class Ids {

        private final UUID tenantId =
                UUID.randomUUID();

        private final UUID teacherProfileId =
                UUID.randomUUID();

        private final UUID academicYearId =
                UUID.randomUUID();

        private final UUID academicTermId =
                UUID.randomUUID();

        private final UUID campusId =
                UUID.randomUUID();

        private final UUID classGradeId =
                UUID.randomUUID();

        private final UUID streamId =
                UUID.randomUUID();

        private final UUID subjectId =
                UUID.randomUUID();
    }

    private static final class Harness {

        private final TeachingAssignmentRepository repository =
                mock(TeachingAssignmentRepository.class);

        private final TeacherProfileRepository teachers =
                mock(TeacherProfileRepository.class);

        private final AcademicYearRepository academicYears =
                mock(AcademicYearRepository.class);

        private final AcademicTermRepository academicTerms =
                mock(AcademicTermRepository.class);

        private final CampusRepository campuses =
                mock(CampusRepository.class);

        private final ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        private final StreamRepository streams =
                mock(StreamRepository.class);

        private final SubjectRepository subjects =
                mock(SubjectRepository.class);

        private final TeachingAssignmentService service =
                new TeachingAssignmentService(
                        repository,
                        teachers,
                        academicYears,
                        academicTerms,
                        campuses,
                        classGrades,
                        streams,
                        subjects
                );

        private void stubReferenceAvailable() {

            when(
                    repository.findByTenantIdAndAssignmentReference(
                            any(UUID.class),
                            anyString()
                    )
            ).thenReturn(
                    Optional.empty()
            );
        }

        private void stubRequired(
                Ids ids
        ) {

            stubReferenceAvailable();

            when(
                    teachers.findByTenantIdAndId(
                            ids.tenantId,
                            ids.teacherProfileId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(TeacherProfile.class)
                    )
            );

            when(
                    academicYears.findByTenantIdAndId(
                            ids.tenantId,
                            ids.academicYearId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(AcademicYear.class)
                    )
            );

            when(
                    campuses.findByTenantIdAndId(
                            ids.tenantId,
                            ids.campusId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Campus.class)
                    )
            );

            when(
                    classGrades.findByTenantIdAndId(
                            ids.tenantId,
                            ids.classGradeId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(ClassGrade.class)
                    )
            );

            when(
                    subjects.findByTenantIdAndId(
                            ids.tenantId,
                            ids.subjectId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Subject.class)
                    )
            );
        }

        private void stubRequiredBeforeStream(
                Ids ids
        ) {

            stubReferenceAvailable();

            when(
                    teachers.findByTenantIdAndId(
                            ids.tenantId,
                            ids.teacherProfileId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(TeacherProfile.class)
                    )
            );

            when(
                    academicYears.findByTenantIdAndId(
                            ids.tenantId,
                            ids.academicYearId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(AcademicYear.class)
                    )
            );

            when(
                    campuses.findByTenantIdAndId(
                            ids.tenantId,
                            ids.campusId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Campus.class)
                    )
            );

            when(
                    classGrades.findByTenantIdAndId(
                            ids.tenantId,
                            ids.classGradeId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(ClassGrade.class)
                    )
            );
        }

        private TeachingAssignment createDefault(
                Ids ids
        ) {

            return service.create(
                    ids.tenantId,
                    "TA-001",
                    ids.teacherProfileId,
                    ids.academicYearId,
                    null,
                    ids.campusId,
                    ids.classGradeId,
                    null,
                    ids.subjectId,
                    "PRIMARY_TEACHER",
                    8,
                    null,
                    LocalDate.of(2026, 2, 1),
                    null,
                    null
            );
        }

        private TeachingAssignment createWithStream(
                Ids ids
        ) {

            return service.create(
                    ids.tenantId,
                    "TA-001",
                    ids.teacherProfileId,
                    ids.academicYearId,
                    null,
                    ids.campusId,
                    ids.classGradeId,
                    ids.streamId,
                    ids.subjectId,
                    "PRIMARY_TEACHER",
                    8,
                    null,
                    LocalDate.of(2026, 2, 1),
                    null,
                    null
            );
        }
    }
}
