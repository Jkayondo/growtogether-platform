package africa.growtogether.platform.school.assessment.examination.registration;

import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidate;
import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidateRepository;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaper;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaperRepository;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationSchedule;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationScheduleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CandidatePaperRegistrationServiceTest {


    @Mock
    private CandidatePaperRegistrationRepository repository;

    @Mock
    private ExaminationCandidateRepository candidateRepository;

    @Mock
    private AssessmentPaperRepository paperRepository;

    @Mock
    private ExaminationScheduleRepository scheduleRepository;


    private CandidatePaperRegistrationService service;

    private UUID tenantId;
    private UUID actorId;
    private UUID candidateId;
    private UUID paperId;
    private UUID scheduleId;
    private UUID sessionId;


    @BeforeEach
    void setUp() {

        service =
                new CandidatePaperRegistrationService(
                        repository,
                        candidateRepository,
                        paperRepository,
                        scheduleRepository
                );

        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        paperId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
    }


    @Test
    void registerPersistsCoherentTenantScopedRegistration()
            throws Exception {

        ExaminationCandidate candidate =
                mock(ExaminationCandidate.class);

        AssessmentPaper paper =
                mock(AssessmentPaper.class);

        ExaminationSchedule schedule =
                mock(ExaminationSchedule.class);


        when(
                candidate.getExaminationSessionId()
        ).thenReturn(
                sessionId
        );

        when(
                paper.getExaminationSessionId()
        ).thenReturn(
                sessionId
        );

        when(
                schedule.getAssessmentPaperId()
        ).thenReturn(
                paperId
        );

        when(
                schedule.getExaminationSessionId()
        ).thenReturn(
                sessionId
        );


        when(
                candidateRepository.findByTenantIdAndId(
                        tenantId,
                        candidateId
                )
        ).thenReturn(
                Optional.of(candidate)
        );

        when(
                paperRepository.findByTenantIdAndId(
                        tenantId,
                        paperId
                )
        ).thenReturn(
                Optional.of(paper)
        );

        when(
                scheduleRepository.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.of(schedule)
        );

        when(
                repository
                        .existsByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                                tenantId,
                                candidateId,
                                paperId
                        )
        ).thenReturn(
                false
        );

        when(
                repository.save(
                        any(CandidatePaperRegistration.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        CandidatePaperRegistration result =
                service.register(
                        tenantId,
                        actorId,
                        new CreateCandidatePaperRegistrationCommand(
                                candidateId,
                                paperId,
                                scheduleId,
                                " optional "
                        )
                );


        assertThat(
                result.getExaminationCandidateId()
        ).isEqualTo(
                candidateId
        );

        assertThat(
                result.getAssessmentPaperId()
        ).isEqualTo(
                paperId
        );

        assertThat(
                result.getExaminationScheduleId()
        ).isEqualTo(
                scheduleId
        );

        assertThat(
                result.getRegistrationType()
        ).isEqualTo(
                "OPTIONAL"
        );

        assertThat(
                result.getRegistrationStatus()
        ).isEqualTo(
                "REGISTERED"
        );

        assertThat(
                result.getRegisteredBy()
        ).isEqualTo(
                actorId
        );

        assertThat(
                result.getRegisteredAt()
        ).isNotNull();


        Method tenantGetter =
                result.getClass()
                        .getMethod(
                                "getTenantId"
                        );

        assertThat(
                tenantGetter.invoke(result)
        ).isEqualTo(
                tenantId
        );


        verify(
                repository
        ).save(
                result
        );
    }


    @Test
    void registrationTypeDefaultsToStandard() {

        stubCandidateAndPaperSameSession();


        when(
                repository
                        .existsByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                                tenantId,
                                candidateId,
                                paperId
                        )
        ).thenReturn(
                false
        );

        when(
                repository.save(
                        any(CandidatePaperRegistration.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        CandidatePaperRegistration result =
                service.register(
                        tenantId,
                        actorId,
                        new CreateCandidatePaperRegistrationCommand(
                                candidateId,
                                paperId,
                                null,
                                null
                        )
                );


        assertThat(
                result.getRegistrationType()
        ).isEqualTo(
                "STANDARD"
        );
    }


    @Test
    void rejectsUnsupportedRegistrationType() {

        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                new CreateCandidatePaperRegistrationCommand(
                                        candidateId,
                                        paperId,
                                        null,
                                        "INVALID"
                                )
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Unsupported candidate paper registration type"
                );


        verifyNoInteractions(
                candidateRepository,
                paperRepository,
                scheduleRepository,
                repository
        );
    }


    @Test
    void rejectsCandidateOutsideTenant() {

        when(
                candidateRepository.findByTenantIdAndId(
                        tenantId,
                        candidateId
                )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithoutSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Examination candidate not found for tenant"
                );


        verifyNoInteractions(
                paperRepository,
                scheduleRepository,
                repository
        );
    }


    @Test
    void rejectsPaperOutsideTenant() {

        ExaminationCandidate candidate =
                mock(ExaminationCandidate.class);

        when(
                candidateRepository.findByTenantIdAndId(
                        tenantId,
                        candidateId
                )
        ).thenReturn(
                Optional.of(candidate)
        );

        when(
                paperRepository.findByTenantIdAndId(
                        tenantId,
                        paperId
                )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithoutSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Assessment paper not found for tenant"
                );
    }


    @Test
    void rejectsPaperFromDifferentExaminationSession() {

        ExaminationCandidate candidate =
                mock(ExaminationCandidate.class);

        AssessmentPaper paper =
                mock(AssessmentPaper.class);


        when(
                candidate.getExaminationSessionId()
        ).thenReturn(
                sessionId
        );

        when(
                paper.getExaminationSessionId()
        ).thenReturn(
                UUID.randomUUID()
        );


        when(
                candidateRepository.findByTenantIdAndId(
                        tenantId,
                        candidateId
                )
        ).thenReturn(
                Optional.of(candidate)
        );

        when(
                paperRepository.findByTenantIdAndId(
                        tenantId,
                        paperId
                )
        ).thenReturn(
                Optional.of(paper)
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithoutSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Assessment paper does not belong to the candidate examination session"
                );
    }


    @Test
    void rejectsScheduleOutsideTenant() {

        stubCandidateAndPaperSameSession();


        when(
                scheduleRepository.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Examination schedule not found for tenant"
                );
    }


    @Test
    void rejectsScheduleForDifferentPaper() {

        stubCandidateAndPaperSameSession();

        ExaminationSchedule schedule =
                mock(ExaminationSchedule.class);


        when(
                schedule.getAssessmentPaperId()
        ).thenReturn(
                UUID.randomUUID()
        );


        when(
                scheduleRepository.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.of(schedule)
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Examination schedule does not belong to the assessment paper"
                );
    }


    @Test
    void rejectsScheduleForDifferentExaminationSession() {

        stubCandidateAndPaperSameSession();

        ExaminationSchedule schedule =
                mock(ExaminationSchedule.class);


        when(
                schedule.getAssessmentPaperId()
        ).thenReturn(
                paperId
        );

        when(
                schedule.getExaminationSessionId()
        ).thenReturn(
                UUID.randomUUID()
        );


        when(
                scheduleRepository.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.of(schedule)
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Examination schedule does not belong to the candidate examination session"
                );
    }


    @Test
    void rejectsDuplicateCandidatePaperRegistration() {

        stubCandidateAndPaperSameSession();


        when(
                repository
                        .existsByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                                tenantId,
                                candidateId,
                                paperId
                        )
        ).thenReturn(
                true
        );


        assertThatThrownBy(
                () ->
                        service.register(
                                tenantId,
                                actorId,
                                commandWithoutSchedule()
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Candidate paper registration already exists"
                );


        verify(
                repository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void getIsTenantScoped() {

        CandidatePaperRegistration registration =
                new CandidatePaperRegistration(
                        candidateId,
                        paperId,
                        null,
                        "STANDARD",
                        actorId
                );


        when(
                repository
                        .findByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                                tenantId,
                                candidateId,
                                paperId
                        )
        ).thenReturn(
                Optional.of(registration)
        );


        assertThat(
                service.get(
                        tenantId,
                        candidateId,
                        paperId
                )
        ).isSameAs(
                registration
        );
    }


    @Test
    void verifyTransitionsRegisteredToVerifiedAndRejectsRepeat() {

        CandidatePaperRegistration registration =
                new CandidatePaperRegistration(
                        candidateId,
                        paperId,
                        null,
                        "STANDARD",
                        actorId
                );


        when(
                repository
                        .findByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                                tenantId,
                                candidateId,
                                paperId
                        )
        ).thenReturn(
                Optional.of(registration)
        );


        CandidatePaperRegistration verified =
                service.verify(
                        tenantId,
                        candidateId,
                        paperId
                );


        assertThat(
                verified.getRegistrationStatus()
        ).isEqualTo(
                "VERIFIED"
        );


        assertThatThrownBy(
                () ->
                        service.verify(
                                tenantId,
                                candidateId,
                                paperId
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Only REGISTERED candidate paper registrations can be verified"
                );
    }


    @Test
    void withdrawalRequiresReasonAndValidLifecycleState() {

        CandidatePaperRegistration registration =
                new CandidatePaperRegistration(
                        candidateId,
                        paperId,
                        null,
                        "STANDARD",
                        actorId
                );


        assertThatThrownBy(
                () ->
                        registration.withdraw(
                                "   "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Withdrawal reason is required"
                );


        registration.withdraw(
                "Candidate changed subject"
        );


        assertThat(
                registration.getRegistrationStatus()
        ).isEqualTo(
                "WITHDRAWN"
        );

        assertThat(
                registration.getWithdrawnAt()
        ).isNotNull();

        assertThat(
                registration.getWithdrawalReason()
        ).isEqualTo(
                "Candidate changed subject"
        );


        assertThatThrownBy(
                () ->
                        registration.withdraw(
                                "Again"
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }


    private void stubCandidateAndPaperSameSession() {

        ExaminationCandidate candidate =
                mock(ExaminationCandidate.class);

        AssessmentPaper paper =
                mock(AssessmentPaper.class);


        when(
                candidate.getExaminationSessionId()
        ).thenReturn(
                sessionId
        );

        when(
                paper.getExaminationSessionId()
        ).thenReturn(
                sessionId
        );


        when(
                candidateRepository.findByTenantIdAndId(
                        tenantId,
                        candidateId
                )
        ).thenReturn(
                Optional.of(candidate)
        );

        when(
                paperRepository.findByTenantIdAndId(
                        tenantId,
                        paperId
                )
        ).thenReturn(
                Optional.of(paper)
        );
    }


    private CreateCandidatePaperRegistrationCommand
    commandWithoutSchedule() {

        return new CreateCandidatePaperRegistrationCommand(
                candidateId,
                paperId,
                null,
                "STANDARD"
        );
    }


    private CreateCandidatePaperRegistrationCommand
    commandWithSchedule() {

        return new CreateCandidatePaperRegistrationCommand(
                candidateId,
                paperId,
                scheduleId,
                "STANDARD"
        );
    }

}
