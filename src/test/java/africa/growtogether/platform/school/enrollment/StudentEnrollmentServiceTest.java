package africa.growtogether.platform.school.enrollment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentEnrollmentServiceTest {

    @Test
    void createsTenantScopedEnrollment() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository.save(
                        any(StudentEnrollment.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        StudentEnrollment result =
                f.create();

        assertNotNull(result);

        assertEquals(
                f.studentId,
                result.getStudentId()
        );

        assertEquals(
                f.academicYearId,
                result.getAcademicYearId()
        );

        assertEquals(
                f.academicTermId,
                result.getAcademicTermId()
        );

        assertEquals(
                f.campusId,
                result.getCampusId()
        );

        assertEquals(
                f.classGradeId,
                result.getClassGradeId()
        );

        assertEquals(
                f.streamId,
                result.getStreamId()
        );

        assertEquals(
                "ENR-2026-001",
                result.getEnrollmentNumber()
        );

        assertEquals(
                "NEW",
                result.getEnrollmentType()
        );

        assertEquals(
                "ACTIVE",
                result.getEnrollmentStatus()
        );

        verify(
                f.repository
        ).save(
                any(StudentEnrollment.class)
        );
    }

    @Test
    void rejectsStudentOutsideTenantBoundary() {

        Fixture f = new Fixture();

        when(
                f.students.findByTenantIdAndId(
                        f.tenantId,
                        f.studentId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        f::create
                );

        assertEquals(
                "Student not found for tenant",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentEnrollment.class)
        );
    }

    @Test
    void rejectsAcademicTermFromDifferentAcademicYear() {

        Fixture f = new Fixture();

        f.stubStudentAndYear();

        AcademicTerm term =
                mock(AcademicTerm.class);

        AcademicYear otherYear =
                mock(AcademicYear.class);

        when(
                otherYear.getId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                term.getAcademicYear()
        ).thenReturn(
                otherYear
        );

        when(
                f.academicTerms.findByTenantIdAndId(
                        f.tenantId,
                        f.academicTermId
                )
        ).thenReturn(
                Optional.of(term)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        f::create
                );

        assertEquals(
                "Academic term does not belong to academic year",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentEnrollment.class)
        );
    }

    @Test
    void rejectsStreamFromDifferentCampus() {

        Fixture f = new Fixture();

        f.stubThroughClassGrade();

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
                f.classGradeId
        );

        when(
                f.streams.findByTenantIdAndId(
                        f.tenantId,
                        f.streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        f::create
                );

        assertEquals(
                "Stream does not belong to campus",
                error.getMessage()
        );
    }

    @Test
    void rejectsDuplicateEnrollmentNumberWithinTenant() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository.existsByTenantIdAndEnrollmentNumber(
                        f.tenantId,
                        "ENR-2026-001"
                )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        f::create
                );

        assertEquals(
                "Enrollment number already exists for tenant",
                error.getMessage()
        );
    }

    @Test
    void rejectsDuplicateEnrollmentForAcademicPeriod() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository
                        .existsByTenantIdAndStudentIdAndAcademicYearIdAndAcademicTermId(
                                f.tenantId,
                                f.studentId,
                                f.academicYearId,
                                f.academicTermId
                        )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        f::create
                );

        assertEquals(
                "Student already has enrollment for academic period",
                error.getMessage()
        );
    }

    @Test
    void rejectsSecondActiveEnrollmentForStudent() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                f.tenantId,
                                f.studentId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(
                        mock(StudentEnrollment.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        f::create
                );

        assertEquals(
                "Student already has an active enrollment",
                error.getMessage()
        );
    }

    @Test
    void rejectsInvalidEnrollmentType() {

        Fixture f = new Fixture();

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.studentId,
                                f.academicYearId,
                                f.academicTermId,
                                f.campusId,
                                f.classGradeId,
                                f.streamId,
                                "ENR-2026-001",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 1),
                                null,
                                "INVALID_TYPE",
                                null,
                                null,
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                "Invalid enrollment type: INVALID_TYPE",
                error.getMessage()
        );

        verifyNoInteractions(
                f.students
        );
    }

    @Test
    void rejectsPreviousEnrollmentBelongingToAnotherStudent() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        UUID previousEnrollmentId =
                UUID.randomUUID();

        StudentEnrollment previous =
                mock(StudentEnrollment.class);

        when(
                previous.getStudentId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        previousEnrollmentId
                )
        ).thenReturn(
                Optional.of(previous)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.studentId,
                                f.academicYearId,
                                f.academicTermId,
                                f.campusId,
                                f.classGradeId,
                                f.streamId,
                                "ENR-2026-001",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 1),
                                null,
                                "CONTINUING",
                                previousEnrollmentId,
                                null,
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                "Previous enrollment belongs to another student",
                error.getMessage()
        );
    }

    private static class Fixture {

        final StudentEnrollmentRepository repository =
                mock(StudentEnrollmentRepository.class);

        final StudentRepository students =
                mock(StudentRepository.class);

        final AcademicYearRepository academicYears =
                mock(AcademicYearRepository.class);

        final AcademicTermRepository academicTerms =
                mock(AcademicTermRepository.class);

        final CampusRepository campuses =
                mock(CampusRepository.class);

        final ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        final StreamRepository streams =
                mock(StreamRepository.class);

        final StudentEnrollmentService service =
                new StudentEnrollmentService(
                        repository,
                        students,
                        academicYears,
                        academicTerms,
                        campuses,
                        classGrades,
                        streams
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID studentId =
                UUID.randomUUID();

        final UUID academicYearId =
                UUID.randomUUID();

        final UUID academicTermId =
                UUID.randomUUID();

        final UUID campusId =
                UUID.randomUUID();

        final UUID classGradeId =
                UUID.randomUUID();

        final UUID streamId =
                UUID.randomUUID();

        void stubStudentAndYear() {

            when(
                    students.findByTenantIdAndId(
                            tenantId,
                            studentId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Student.class)
                    )
            );

            AcademicYear academicYear =
                    mock(AcademicYear.class);

            when(
                    academicYear.getId()
            ).thenReturn(
                    academicYearId
            );

            when(
                    academicYears.findByTenantIdAndId(
                            tenantId,
                            academicYearId
                    )
            ).thenReturn(
                    Optional.of(academicYear)
            );
        }

        void stubThroughClassGrade() {

            stubStudentAndYear();

            AcademicYear academicYear =
                    mock(AcademicYear.class);

            when(
                    academicYear.getId()
            ).thenReturn(
                    academicYearId
            );

            AcademicTerm term =
                    mock(AcademicTerm.class);

            when(
                    term.getAcademicYear()
            ).thenReturn(
                    academicYear
            );

            when(
                    academicTerms.findByTenantIdAndId(
                            tenantId,
                            academicTermId
                    )
            ).thenReturn(
                    Optional.of(term)
            );

            when(
                    campuses.findByTenantIdAndId(
                            tenantId,
                            campusId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Campus.class)
                    )
            );

            when(
                    classGrades.findByTenantIdAndId(
                            tenantId,
                            classGradeId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(ClassGrade.class)
                    )
            );
        }

        void stubValidDependencies() {

            stubThroughClassGrade();

            Stream stream =
                    mock(Stream.class);

            when(
                    stream.getCampusId()
            ).thenReturn(
                    campusId
            );

            when(
                    stream.getClassGradeId()
            ).thenReturn(
                    classGradeId
            );

            when(
                    streams.findByTenantIdAndId(
                            tenantId,
                            streamId
                    )
            ).thenReturn(
                    Optional.of(stream)
            );

            when(
                    repository.existsByTenantIdAndEnrollmentNumber(
                            tenantId,
                            "ENR-2026-001"
                    )
            ).thenReturn(
                    false
            );

            when(
                    repository
                            .existsByTenantIdAndStudentIdAndAcademicYearIdAndAcademicTermId(
                                    tenantId,
                                    studentId,
                                    academicYearId,
                                    academicTermId
                            )
            ).thenReturn(
                    false
            );

            when(
                    repository
                            .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                    tenantId,
                                    studentId,
                                    "ACTIVE",
                                    EntityStatus.ACTIVE
                            )
            ).thenReturn(
                    Optional.empty()
            );
        }

        StudentEnrollment create() {

            return service.create(
                    tenantId,
                    studentId,
                    academicYearId,
                    academicTermId,
                    campusId,
                    classGradeId,
                    streamId,
                    "ENR-2026-001",
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 2, 1),
                    null,
                    "NEW",
                    null,
                    null,
                    UUID.randomUUID()
            );
        }
    }

    @Test
    void marksEnrollmentPending() {

        Fixture f = new Fixture();

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        f.studentId,
                        f.academicYearId,
                        f.academicTermId,
                        f.campusId,
                        f.classGradeId,
                        f.streamId,
                        "ENR-LIFE-001",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 1),
                        null,
                        "NEW",
                        null,
                        null,
                        UUID.randomUUID()
                );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        UUID.fromString(
                                "00000000-0000-0000-0000-000000000001"
                        )
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                f.repository.save(enrollment)
        ).thenReturn(enrollment);

        StudentEnrollment result =
                f.service.markPending(
                        f.tenantId,
                        UUID.fromString(
                                "00000000-0000-0000-0000-000000000001"
                        )
                );

        assertEquals(
                "PENDING",
                result.getEnrollmentStatus()
        );
    }

    @Test
    void activatesEnrollmentAndRecordsApproval() {

        Fixture f = new Fixture();

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        f.studentId,
                        f.academicYearId,
                        f.academicTermId,
                        f.campusId,
                        f.classGradeId,
                        f.streamId,
                        "ENR-LIFE-002",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 1),
                        null,
                        "NEW",
                        null,
                        null,
                        UUID.randomUUID()
                );

        enrollment.markPending();

        UUID enrollmentId =
                UUID.randomUUID();

        UUID approvedBy =
                UUID.randomUUID();

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        enrollmentId
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                f.repository
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                f.tenantId,
                                f.studentId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                f.repository.save(enrollment)
        ).thenReturn(enrollment);

        StudentEnrollment result =
                f.service.activate(
                        f.tenantId,
                        enrollmentId,
                        approvedBy
                );

        assertEquals(
                "ACTIVE",
                result.getEnrollmentStatus()
        );

        assertEquals(
                approvedBy,
                result.getApprovedBy()
        );

        assertNotNull(
                result.getApprovedAt()
        );
    }

    @Test
    void rejectsActivationWithoutApprover() {

        Fixture f = new Fixture();

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.activate(
                                f.tenantId,
                                UUID.randomUUID(),
                                null
                        )
                );

        assertEquals(
                "approvedBy must not be null",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).findByTenantIdAndId(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void suspendsEnrollment() {

        Fixture f = new Fixture();

        UUID enrollmentId =
                UUID.randomUUID();

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        f.studentId,
                        f.academicYearId,
                        f.academicTermId,
                        f.campusId,
                        f.classGradeId,
                        f.streamId,
                        "ENR-LIFE-003",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 1),
                        null,
                        "NEW",
                        null,
                        null,
                        UUID.randomUUID()
                );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        enrollmentId
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                f.repository.save(enrollment)
        ).thenReturn(enrollment);

        StudentEnrollment result =
                f.service.suspend(
                        f.tenantId,
                        enrollmentId
                );

        assertEquals(
                "SUSPENDED",
                result.getEnrollmentStatus()
        );
    }

    @Test
    void completesEnrollmentWithEffectiveEndDate() {

        Fixture f = new Fixture();

        UUID enrollmentId =
                UUID.randomUUID();

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        f.studentId,
                        f.academicYearId,
                        f.academicTermId,
                        f.campusId,
                        f.classGradeId,
                        f.streamId,
                        "ENR-LIFE-004",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 1),
                        null,
                        "NEW",
                        null,
                        null,
                        UUID.randomUUID()
                );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        enrollmentId
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                f.repository.save(enrollment)
        ).thenReturn(enrollment);

        LocalDate completedOn =
                LocalDate.of(2026, 12, 5);

        StudentEnrollment result =
                f.service.complete(
                        f.tenantId,
                        enrollmentId,
                        completedOn
                );

        assertEquals(
                "COMPLETED",
                result.getEnrollmentStatus()
        );

        assertEquals(
                completedOn,
                result.getEffectiveTo()
        );
    }

    @Test
    void withdrawsEnrollmentAndRecordsExit() {

        Fixture f = new Fixture();

        UUID enrollmentId =
                UUID.randomUUID();

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        f.studentId,
                        f.academicYearId,
                        f.academicTermId,
                        f.campusId,
                        f.classGradeId,
                        f.streamId,
                        "ENR-LIFE-005",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 1),
                        null,
                        "NEW",
                        null,
                        null,
                        UUID.randomUUID()
                );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        enrollmentId
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                f.repository.save(enrollment)
        ).thenReturn(enrollment);

        LocalDate exitDate =
                LocalDate.of(2026, 6, 15);

        StudentEnrollment result =
                f.service.withdraw(
                        f.tenantId,
                        enrollmentId,
                        exitDate,
                        "Parent requested withdrawal"
                );

        assertEquals(
                "WITHDRAWN",
                result.getEnrollmentStatus()
        );

        assertEquals(
                exitDate,
                result.getExitDate()
        );

        assertEquals(
                "Parent requested withdrawal",
                result.getExitReason()
        );
    }

    @Test
    void cancelsEnrollment() {

        Fixture f = new Fixture();

        UUID enrollmentId =
                UUID.randomUUID();

        StudentEnrollment enrollment =
                new StudentEnrollment(
                        f.studentId,
                        f.academicYearId,
                        f.academicTermId,
                        f.campusId,
                        f.classGradeId,
                        f.streamId,
                        "ENR-LIFE-006",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 1),
                        null,
                        "NEW",
                        null,
                        null,
                        UUID.randomUUID()
                );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        enrollmentId
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                f.repository.save(enrollment)
        ).thenReturn(enrollment);

        StudentEnrollment result =
                f.service.cancel(
                        f.tenantId,
                        enrollmentId
                );

        assertEquals(
                "CANCELLED",
                result.getEnrollmentStatus()
        );
    }

}
