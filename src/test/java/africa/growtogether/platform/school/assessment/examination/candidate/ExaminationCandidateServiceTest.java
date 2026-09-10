package africa.growtogether.platform.school.assessment.examination.candidate;

import africa.growtogether.platform.school.assessment.examination.ExaminationSession;
import africa.growtogether.platform.school.assessment.examination.ExaminationSessionRepository;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExaminationCandidateServiceTest {


    @Mock
    private ExaminationCandidateRepository repository;

    @Mock
    private ExaminationSessionRepository sessions;

    @Mock
    private StudentRepository students;

    @Mock
    private StudentEnrollmentRepository enrollments;

    @Mock
    private ExaminationSession session;

    @Mock
    private Student student;

    @Mock
    private StudentEnrollment enrollment;


    private ExaminationCandidateService service;

    private UUID tenantId;
    private UUID examinationSessionId;
    private UUID studentId;
    private UUID studentEnrollmentId;
    private UUID academicYearId;
    private UUID campusId;


    @BeforeEach
    void setUp() {

        service =
                new ExaminationCandidateService(
                        repository,
                        sessions,
                        students,
                        enrollments
                );

        tenantId = UUID.randomUUID();
        examinationSessionId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        studentEnrollmentId = UUID.randomUUID();
        academicYearId = UUID.randomUUID();
        campusId = UUID.randomUUID();
    }


    @Test
    void registersCandidateWhenTenantReferencesAndSessionAreValid() {

        stubValidReferences();

        when(
                repository.save(
                        any(ExaminationCandidate.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        ExaminationCandidate candidate =
                service.register(
                        tenantId,
                        "  CAND-001  ",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                );

        assertThat(
                candidate.getCandidateNumber()
        ).isEqualTo(
                "CAND-001"
        );

        assertThat(
                candidate.getExaminationSessionId()
        ).isEqualTo(
                examinationSessionId
        );

        assertThat(
                candidate.getStudentId()
        ).isEqualTo(
                studentId
        );

        assertThat(
                candidate.getStudentEnrollmentId()
        ).isEqualTo(
                studentEnrollmentId
        );

        assertThat(
                candidate.getEligibilityStatus()
        ).isEqualTo(
                "PENDING"
        );

        assertThat(
                candidate.getCandidateStatus()
        ).isEqualTo(
                "REGISTERED"
        );

        verify(repository).save(
                any(ExaminationCandidate.class)
        );
    }


    @Test
    void rejectsCandidateWhenSessionIsOutsideTenant() {

        when(
                sessions.findByTenantIdAndId(
                        tenantId,
                        examinationSessionId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-002",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Examination session not found for tenant"
                );
    }


    @Test
    void rejectsCandidateWhenRegistrationIsNotOpen() {

        when(
                sessions.findByTenantIdAndId(
                        tenantId,
                        examinationSessionId
                )
        ).thenReturn(
                Optional.of(session)
        );

        when(
                session.getSessionStatus()
        ).thenReturn(
                "APPROVED"
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-003",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Examination session is not open for registration"
                );
    }


    @Test
    void rejectsCandidateWhenStudentIsOutsideTenant() {

        stubSession();

        when(
                students.findByTenantIdAndId(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-004",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Student not found for tenant"
                );
    }


    @Test
    void rejectsCandidateWhenEnrollmentIsOutsideTenant() {

        stubSession();

        when(
                students.findByTenantIdAndId(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                Optional.of(student)
        );

        when(
                enrollments.findByTenantIdAndId(
                        tenantId,
                        studentEnrollmentId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-005",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Student enrollment not found for tenant"
                );
    }


    @Test
    void rejectsCandidateWhenEnrollmentBelongsToDifferentStudent() {

        stubValidReferences();

        when(
                enrollment.getStudentId()
        ).thenReturn(
                UUID.randomUUID()
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-006",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Student enrollment does not belong to student"
                );
    }


    @Test
    void rejectsCandidateWhenEnrollmentAcademicYearDiffersFromSession() {

        stubValidReferences();

        when(
                enrollment.getAcademicYearId()
        ).thenReturn(
                UUID.randomUUID()
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-007",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Student enrollment does not belong to examination academic year"
                );
    }


    @Test
    void rejectsCandidateWhenEnrollmentCampusDiffersFromSession() {

        stubValidReferences();

        when(
                enrollment.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-008",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Student enrollment does not belong to examination campus"
                );
    }


    @Test
    void rejectsDuplicateCandidateNumberWithinTenant() {

        stubValidReferences();

        when(
                repository.existsByTenantIdAndCandidateNumber(
                        tenantId,
                        "CAND-009"
                )
        ).thenReturn(
                true
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-009",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Candidate number already exists"
                );
    }


    @Test
    void rejectsDuplicateStudentWithinExaminationSession() {

        stubValidReferences();

        when(
                repository
                        .existsByTenantIdAndExaminationSessionIdAndStudentId(
                                tenantId,
                                examinationSessionId,
                                studentId
                        )
        ).thenReturn(
                true
        );

        assertThatThrownBy(
                () -> service.register(
                        tenantId,
                        "CAND-010",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Student already registered for examination session"
                );
    }


    private void stubSession() {

        when(
                sessions.findByTenantIdAndId(
                        tenantId,
                        examinationSessionId
                )
        ).thenReturn(
                Optional.of(session)
        );

        when(
                session.getSessionStatus()
        ).thenReturn(
                "REGISTRATION_OPEN"
        );

        when(
                session.getAcademicYearId()
        ).thenReturn(
                academicYearId
        );

        when(
                session.getCampusId()
        ).thenReturn(
                campusId
        );
    }


    private void stubValidReferences() {

        stubSession();

        when(
                students.findByTenantIdAndId(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                Optional.of(student)
        );

        when(
                enrollments.findByTenantIdAndId(
                        tenantId,
                        studentEnrollmentId
                )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(
                enrollment.getStudentId()
        ).thenReturn(
                studentId
        );

        when(
                enrollment.getAcademicYearId()
        ).thenReturn(
                academicYearId
        );

        when(
                enrollment.getCampusId()
        ).thenReturn(
                campusId
        );

        when(
                repository.existsByTenantIdAndCandidateNumber(
                        tenantId,
                        "CAND-001"
                )
        ).thenReturn(
                false
        );

        when(
                repository
                        .existsByTenantIdAndExaminationSessionIdAndStudentId(
                                tenantId,
                                examinationSessionId,
                                studentId
                        )
        ).thenReturn(
                false
        );
    }

}
