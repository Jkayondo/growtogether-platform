package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidateRepository;
import africa.growtogether.platform.school.assessment.examination.registration.CandidatePaperRegistrationRepository;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CandidateScoreServiceTest {


    private final UUID tenant =
            UUID.randomUUID();

    private final UUID sheetId =
            UUID.randomUUID();

    private final UUID classOfferingId =
            UUID.randomUUID();

    private final UUID studentId =
            UUID.randomUUID();

    private final UUID enrollmentId =
            UUID.randomUUID();

    private final UUID academicYearId =
            UUID.randomUUID();

    private final UUID campusId =
            UUID.randomUUID();

    private final UUID classGradeId =
            UUID.randomUUID();

    private final UUID actor =
            UUID.randomUUID();


    @Mock
    private CandidateScoreRepository repository;

    @Mock
    private MarkSheetRepository markSheets;

    @Mock
    private ClassOfferingRepository classOfferings;

    @Mock
    private StudentRepository students;

    @Mock
    private StudentEnrollmentRepository enrollments;

    @Mock
    private ExaminationCandidateRepository examinationCandidates;

    @Mock
    private CandidatePaperRegistrationRepository
            candidatePaperRegistrations;


    private CandidateScoreService service;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(
                this
        );

        service =
                new CandidateScoreService(
                        repository,
                        markSheets,
                        classOfferings,
                        students,
                        enrollments,
                        examinationCandidates,
                        candidatePaperRegistrations
                );
    }


    @Test
    void createsTenantScopedScoreForMatchingOpenSheetEnrollment() {

        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );

        ClassOffering classOffering =
                mock(
                        ClassOffering.class
                );

        Student student =
                mock(
                        Student.class
                );

        StudentEnrollment enrollment =
                mock(
                        StudentEnrollment.class
                );


        when(
                markSheets.findByTenantIdAndId(
                        tenant,
                        sheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );

        when(
                sheet.getMarkSheetStatus()
        )
                .thenReturn(
                        "OPEN"
                );

        when(
                sheet.getClassOfferingId()
        )
                .thenReturn(
                        classOfferingId
                );

        when(
                sheet.getStreamId()
        )
                .thenReturn(
                        null
                );


        when(
                students.findByTenantIdAndId(
                        tenant,
                        studentId
                )
        )
                .thenReturn(
                        Optional.of(
                                student
                        )
                );


        when(
                enrollments.findByTenantIdAndId(
                        tenant,
                        enrollmentId
                )
        )
                .thenReturn(
                        Optional.of(
                                enrollment
                        )
                );

        when(
                enrollment.getStudentId()
        )
                .thenReturn(
                        studentId
                );

        when(
                enrollment.getEnrollmentStatus()
        )
                .thenReturn(
                        "ACTIVE"
                );

        when(
                enrollment.getAcademicYearId()
        )
                .thenReturn(
                        academicYearId
                );

        when(
                enrollment.getCampusId()
        )
                .thenReturn(
                        campusId
                );


        when(
                enrollment.getClassGradeId()
        )
                .thenReturn(
                        classGradeId
                );


        when(
                classOfferings.findByTenantIdAndId(
                        tenant,
                        classOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                classOffering
                        )
                );

        when(
                classOffering.getAcademicYearId()
        )
                .thenReturn(
                        academicYearId
                );

        when(
                classOffering.getCampusId()
        )
                .thenReturn(
                        campusId
                );


        when(
                classOffering.getClassGradeId()
        )
                .thenReturn(
                        classGradeId
                );


        when(
                repository.existsByTenantIdAndMarkSheetIdAndStudentId(
                        tenant,
                        sheetId,
                        studentId
                )
        )
                .thenReturn(
                        false
                );


        when(
                repository.save(
                        any(
                                CandidateScore.class
                        )
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(
                                        0
                                )
                );


        CandidateScore created =
                service.create(
                        tenant,
                        new CreateCandidateScoreCommand(
                                sheetId,
                                null,
                                null,
                                studentId,
                                enrollmentId
                        )
                );


        assertEquals(
                tenant,
                created.getTenantId()
        );

        assertEquals(
                sheetId,
                created.getMarkSheetId()
        );

        assertEquals(
                studentId,
                created.getStudentId()
        );

        assertEquals(
                enrollmentId,
                created.getStudentEnrollmentId()
        );

        assertEquals(
                "DRAFT",
                created.getScoreStatus()
        );
    }


    @Test
    void rejectsEnrollmentFromDifferentClassGrade() {

        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );

        ClassOffering classOffering =
                mock(
                        ClassOffering.class
                );

        Student student =
                mock(
                        Student.class
                );

        StudentEnrollment enrollment =
                mock(
                        StudentEnrollment.class
                );


        when(
                markSheets.findByTenantIdAndId(
                        tenant,
                        sheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );

        when(
                sheet.getMarkSheetStatus()
        )
                .thenReturn(
                        "OPEN"
                );

        when(
                sheet.getClassOfferingId()
        )
                .thenReturn(
                        classOfferingId
                );


        when(
                students.findByTenantIdAndId(
                        tenant,
                        studentId
                )
        )
                .thenReturn(
                        Optional.of(
                                student
                        )
                );


        when(
                enrollments.findByTenantIdAndId(
                        tenant,
                        enrollmentId
                )
        )
                .thenReturn(
                        Optional.of(
                                enrollment
                        )
                );

        when(
                enrollment.getStudentId()
        )
                .thenReturn(
                        studentId
                );

        when(
                enrollment.getEnrollmentStatus()
        )
                .thenReturn(
                        "ACTIVE"
                );

        when(
                enrollment.getAcademicYearId()
        )
                .thenReturn(
                        academicYearId
                );

        when(
                enrollment.getClassGradeId()
        )
                .thenReturn(
                        UUID.randomUUID()
                );


        when(
                classOfferings.findByTenantIdAndId(
                        tenant,
                        classOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                classOffering
                        )
                );

        when(
                classOffering.getAcademicYearId()
        )
                .thenReturn(
                        academicYearId
                );

        when(
                classOffering.getClassGradeId()
        )
                .thenReturn(
                        classGradeId
                );


        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        tenant,
                        new CreateCandidateScoreCommand(
                                sheetId,
                                null,
                                null,
                                studentId,
                                enrollmentId
                        )
                )
        );

        verify(
                repository,
                never()
        )
                .save(
                        any()
                );
    }


    @Test
    void rejectsEnrollmentFromDifferentCampus() {

        UUID otherCampusId =
                UUID.randomUUID();


        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );

        ClassOffering classOffering =
                mock(
                        ClassOffering.class
                );

        Student student =
                mock(
                        Student.class
                );

        StudentEnrollment enrollment =
                mock(
                        StudentEnrollment.class
                );


        when(
                markSheets.findByTenantIdAndId(
                        tenant,
                        sheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );

        when(
                sheet.getMarkSheetStatus()
        )
                .thenReturn(
                        "OPEN"
                );

        when(
                sheet.getClassOfferingId()
        )
                .thenReturn(
                        classOfferingId
                );

        when(
                sheet.getStreamId()
        )
                .thenReturn(
                        null
                );


        when(
                students.findByTenantIdAndId(
                        tenant,
                        studentId
                )
        )
                .thenReturn(
                        Optional.of(
                                student
                        )
                );


        when(
                enrollments.findByTenantIdAndId(
                        tenant,
                        enrollmentId
                )
        )
                .thenReturn(
                        Optional.of(
                                enrollment
                        )
                );

        when(
                enrollment.getStudentId()
        )
                .thenReturn(
                        studentId
                );

        when(
                enrollment.getEnrollmentStatus()
        )
                .thenReturn(
                        "ACTIVE"
                );

        when(
                enrollment.getAcademicYearId()
        )
                .thenReturn(
                        academicYearId
                );

        when(
                enrollment.getCampusId()
        )
                .thenReturn(
                        otherCampusId
                );

        when(
                enrollment.getClassGradeId()
        )
                .thenReturn(
                        classGradeId
                );


        when(
                classOfferings.findByTenantIdAndId(
                        tenant,
                        classOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                classOffering
                        )
                );

        when(
                classOffering.getAcademicYearId()
        )
                .thenReturn(
                        academicYearId
                );

        when(
                classOffering.getCampusId()
        )
                .thenReturn(
                        campusId
                );

        when(
                classOffering.getClassGradeId()
        )
                .thenReturn(
                        classGradeId
                );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenant,
                                new CreateCandidateScoreCommand(
                                        sheetId,
                                        null,
                                        null,
                                        studentId,
                                        enrollmentId
                                )
                        )
                );


        assertEquals(
                "Student enrollment campus does not match Mark Sheet",
                exception.getMessage()
        );


        verify(
                repository,
                never()
        )
                .save(
                        any()
                );
    }


    @Test
    void getUsesTenantScopedRepositoryLookup() {

        UUID scoreId =
                UUID.randomUUID();

        CandidateScore score =
                mock(
                        CandidateScore.class
                );

        when(
                repository.findByTenantIdAndId(
                        tenant,
                        scoreId
                )
        )
                .thenReturn(
                        Optional.of(
                                score
                        )
                );


        assertSame(
                score,
                service.get(
                        tenant,
                        scoreId
                )
        );

        verify(
                repository
        )
                .findByTenantIdAndId(
                        tenant,
                        scoreId
                );

        verify(
                repository,
                never()
        )
                .findById(
                        any()
                );
    }


    @Test
    void enterScoreRejectsValueAboveMarkSheetMaximum() {

        UUID scoreId =
                UUID.randomUUID();

        CandidateScore candidateScore =
                new CandidateScore(
                        sheetId,
                        studentId,
                        enrollmentId
                );

        candidateScore.setTenantId(
                tenant
        );


        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );


        when(
                repository.findByTenantIdAndId(
                        tenant,
                        scoreId
                )
        )
                .thenReturn(
                        Optional.of(
                                candidateScore
                        )
                );

        when(
                markSheets.findByTenantIdAndId(
                        tenant,
                        sheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );

        when(
                sheet.getMarkSheetStatus()
        )
                .thenReturn(
                        "OPEN"
                );

        when(
                sheet.getMaximumScore()
        )
                .thenReturn(
                        new BigDecimal(
                                "100"
                        )
                );


        assertThrows(
                IllegalArgumentException.class,
                () -> service.enterScore(
                        tenant,
                        scoreId,
                        new BigDecimal(
                                "101"
                        ),
                        actor
                )
        );

        assertEquals(
                "DRAFT",
                candidateScore.getScoreStatus()
        );
    }


    @Test
    void markAbsentProducesDatabaseCompatibleEnteredState() {

        UUID scoreId =
                UUID.randomUUID();

        CandidateScore candidateScore =
                new CandidateScore(
                        sheetId,
                        studentId,
                        enrollmentId
                );

        candidateScore.setTenantId(
                tenant
        );


        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );


        when(
                repository.findByTenantIdAndId(
                        tenant,
                        scoreId
                )
        )
                .thenReturn(
                        Optional.of(
                                candidateScore
                        )
                );

        when(
                markSheets.findByTenantIdAndId(
                        tenant,
                        sheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );

        when(
                sheet.getMarkSheetStatus()
        )
                .thenReturn(
                        "OPEN"
                );


        CandidateScore result =
                service.markAbsent(
                        tenant,
                        scoreId,
                        actor
                );


        assertTrue(
                result.isAbsent()
        );

        assertNull(
                result.getFinalScore()
        );

        assertEquals(
                "ENTERED",
                result.getScoreStatus()
        );
    }
}
