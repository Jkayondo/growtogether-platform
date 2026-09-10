package africa.growtogether.platform.school.assessment.examination;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ExaminationSessionServiceTest {

    @Mock
    ExaminationSessionRepository repository;

    @Mock
    AcademicYearRepository academicYears;

    @Mock
    AcademicTermRepository academicTerms;

    @Mock
    CampusRepository campuses;

    ExaminationSessionService service;

    UUID tenantId;
    UUID academicYearId;
    UUID campusId;


    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        academicYearId = UUID.randomUUID();
        campusId = UUID.randomUUID();

        service = new ExaminationSessionService(
                repository,
                academicYears,
                academicTerms,
                campuses
        );
    }


    @Test
    void createsTenantScopedDraftSession() {

        CreateExaminationSessionCommand command =
                command(null);

        when(
                repository.existsByTenantIdAndSessionCode(
                        tenantId,
                        command.sessionCode()
                )
        ).thenReturn(false);

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
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
                repository.save(
                        any(ExaminationSession.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        ExaminationSession result =
                service.create(
                        tenantId,
                        command
                );


        assertThat(result.getTenantId())
                .isEqualTo(tenantId);

        assertThat(result.getSessionCode())
                .isEqualTo("EX-2026-001");

        assertThat(result.getSessionStatus())
                .isEqualTo("DRAFT");

        assertThat(result.getDescription())
                .isEqualTo("Controlled examination session.");

        verify(repository).save(
                any(ExaminationSession.class)
        );
    }


    @Test
    void rejectsDuplicateSessionCode() {

        CreateExaminationSessionCommand command =
                command(null);

        when(
                repository.existsByTenantIdAndSessionCode(
                        tenantId,
                        command.sessionCode()
                )
        ).thenReturn(true);


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                command
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "already exists"
                );


        verify(repository, never()).save(any());
    }


    @Test
    void rejectsAcademicYearOutsideTenant() {

        CreateExaminationSessionCommand command =
                command(null);

        when(
                repository.existsByTenantIdAndSessionCode(
                        tenantId,
                        command.sessionCode()
                )
        ).thenReturn(false);

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(Optional.empty());


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                command
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Academic year"
                );
    }


    @Test
    void rejectsAcademicTermOutsideTenant() {

        UUID academicTermId =
                UUID.randomUUID();

        CreateExaminationSessionCommand command =
                command(
                        academicTermId
                );

        when(
                repository.existsByTenantIdAndSessionCode(
                        tenantId,
                        command.sessionCode()
                )
        ).thenReturn(false);

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                academicTerms.findByTenantIdAndId(
                        tenantId,
                        academicTermId
                )
        ).thenReturn(Optional.empty());


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                command
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Academic term"
                );
    }


    @Test
    void rejectsCampusOutsideTenant() {

        CreateExaminationSessionCommand command =
                command(null);

        when(
                repository.existsByTenantIdAndSessionCode(
                        tenantId,
                        command.sessionCode()
                )
        ).thenReturn(false);

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(Optional.empty());


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                command
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Campus"
                );
    }


    @Test
    void rejectsUnsupportedExaminationType() {

        CreateExaminationSessionCommand base =
                command(null);

        CreateExaminationSessionCommand invalid =
                new CreateExaminationSessionCommand(
                        base.sessionCode(),
                        base.sessionName(),
                        base.description(),
                        base.academicYearId(),
                        base.academicTermId(),
                        base.campusId(),
                        "INVALID",
                        base.startDate(),
                        base.endDate(),
                        base.registrationOpenDate(),
                        base.registrationCloseDate(),
                        base.externalAuthority(),
                        base.externalSessionReference(),
                        base.workflowInstanceId()
                );


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                invalid
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Unsupported examination type"
                );
    }


    @Test
    void rejectsEndDateBeforeStartDate() {

        CreateExaminationSessionCommand base =
                command(null);

        CreateExaminationSessionCommand invalid =
                new CreateExaminationSessionCommand(
                        base.sessionCode(),
                        base.sessionName(),
                        base.description(),
                        base.academicYearId(),
                        null,
                        base.campusId(),
                        base.examinationType(),
                        LocalDate.of(2026, 9, 30),
                        LocalDate.of(2026, 9, 1),
                        null,
                        null,
                        null,
                        null,
                        null
                );


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                invalid
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "end date"
                );
    }


    @Test
    void rejectsRegistrationCloseBeforeOpen() {

        CreateExaminationSessionCommand base =
                command(null);

        CreateExaminationSessionCommand invalid =
                new CreateExaminationSessionCommand(
                        base.sessionCode(),
                        base.sessionName(),
                        base.description(),
                        base.academicYearId(),
                        null,
                        base.campusId(),
                        base.examinationType(),
                        base.startDate(),
                        base.endDate(),
                        LocalDate.of(2026, 9, 20),
                        LocalDate.of(2026, 9, 10),
                        null,
                        null,
                        null
                );


        assertThatThrownBy(
                () ->
                        service.create(
                                tenantId,
                                invalid
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Registration close date"
                );
    }


    @Test
    void getUsesTenantScopedRepositoryLookup() {

        UUID id =
                UUID.randomUUID();

        ExaminationSession session =
                session();

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        id
                )
        ).thenReturn(
                Optional.of(session)
        );


        assertThat(
                service.get(
                        tenantId,
                        id
                )
        ).isSameAs(session);


        verify(repository)
                .findByTenantIdAndId(
                        tenantId,
                        id
                );
    }


    @Test
    void getByCodeUsesTenantScopedRepositoryLookup() {

        ExaminationSession session =
                session();

        when(
                repository.findByTenantIdAndSessionCode(
                        tenantId,
                        "EX-2026-001"
                )
        ).thenReturn(
                Optional.of(session)
        );


        assertThat(
                service.getByCode(
                        tenantId,
                        "EX-2026-001"
                )
        ).isSameAs(session);
    }


    @Test
    void followsControlledLifecycle() {

        UUID id =
                UUID.randomUUID();

        UUID approvedBy =
                UUID.randomUUID();

        ExaminationSession session =
                session();

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        id
                )
        ).thenReturn(
                Optional.of(session)
        );


        service.approve(
                tenantId,
                id,
                approvedBy
        );

        assertThat(session.getSessionStatus())
                .isEqualTo("APPROVED");

        assertThat(session.getApprovedBy())
                .isEqualTo(approvedBy);

        assertThat(session.getApprovedAt())
                .isNotNull();


        service.openRegistration(
                tenantId,
                id
        );

        assertThat(session.getSessionStatus())
                .isEqualTo("REGISTRATION_OPEN");


        service.activate(
                tenantId,
                id
        );

        assertThat(session.getSessionStatus())
                .isEqualTo("ACTIVE");


        service.complete(
                tenantId,
                id
        );

        assertThat(session.getSessionStatus())
                .isEqualTo("COMPLETED");
    }


    @Test
    void rejectsOpeningRegistrationFromDraft() {

        UUID id =
                UUID.randomUUID();

        ExaminationSession session =
                session();

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        id
                )
        ).thenReturn(
                Optional.of(session)
        );


        assertThatThrownBy(
                () ->
                        service.openRegistration(
                                tenantId,
                                id
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "Expected APPROVED"
                );
    }


    private ExaminationSession session() {

        ExaminationSession session =
                new ExaminationSession(
                        "EX-2026-001",
                        "Term One Examination",
                        academicYearId,
                        campusId,
                        "INTERNAL",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                );

        session.setTenantId(
                tenantId
        );

        return session;
    }


    private CreateExaminationSessionCommand command(
            UUID academicTermId
    ) {

        return new CreateExaminationSessionCommand(
                "EX-2026-001",
                "Term One Examination",
                "Controlled examination session.",
                academicYearId,
                academicTermId,
                campusId,
                "INTERNAL",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 31),
                null,
                null,
                null
        );
    }
}
