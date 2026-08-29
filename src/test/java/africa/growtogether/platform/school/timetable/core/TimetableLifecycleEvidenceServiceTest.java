package africa.growtogether.platform.school.timetable.core;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;
import africa.growtogether.platform.school.timetable.reliability.TimetableChangeHistoryService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TimetableLifecycleEvidenceServiceTest {

    private final TimetableRepository repository =
            mock(TimetableRepository.class);

    private final AcademicYearRepository academicYears =
            mock(AcademicYearRepository.class);

    private final AcademicTermRepository academicTerms =
            mock(AcademicTermRepository.class);

    private final CampusRepository campuses =
            mock(CampusRepository.class);

    private final BellScheduleRepository bellSchedules =
            mock(BellScheduleRepository.class);

    private final TimetableChangeHistoryService history =
            mock(TimetableChangeHistoryService.class);

    private final TimetableService service =
            new TimetableService(
                    repository,
                    academicYears,
                    academicTerms,
                    campuses,
                    bellSchedules,
                    history
            );


    @AfterEach
    void clearRequestContext() {

        RequestContextHolder.clear();
    }


    @Test
    void submitForReviewRecordsHumanEvidence() {

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID actor = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();

        Timetable timetable =
                timetable(
                        tenantId,
                        timetableId,
                        workflowId
                );

        RequestContextHolder.set(
                new RequestContext(
                        "A11-SUBMIT-001",
                        tenantId.toString()
                )
        );

        service.submitForReview(
                tenantId,
                timetableId,
                actor
        );

        verify(
                timetable
        ).submitForReview();

        verify(
                history
        ).record(
                tenantId,
                timetableId,
                null,
                "TIMETABLE_SUBMITTED_FOR_REVIEW",
                "Timetable submitted for human review",
                actor,
                workflowId,
                "A11-SUBMIT-001",
                false,
                actor.toString()
        );
    }


    @Test
    void approveRecordsApproverEvidence() {

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID actor = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();

        Timetable timetable =
                timetable(
                        tenantId,
                        timetableId,
                        workflowId
                );

        service.approve(
                tenantId,
                timetableId,
                actor
        );

        verify(
                timetable
        ).approve(
                actor
        );

        verify(
                history
        ).record(
                tenantId,
                timetableId,
                null,
                "TIMETABLE_APPROVED",
                "Timetable approved by authorised human reviewer",
                actor,
                workflowId,
                null,
                false,
                actor.toString()
        );
    }


    @Test
    void publishRecordsPublisherEvidence() {

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID actor = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();

        Timetable timetable =
                timetable(
                        tenantId,
                        timetableId,
                        workflowId
                );

        service.publish(
                tenantId,
                timetableId,
                actor
        );

        verify(
                timetable
        ).publish(
                actor
        );

        verify(
                history
        ).record(
                tenantId,
                timetableId,
                null,
                "TIMETABLE_PUBLISHED",
                "Approved timetable published",
                actor,
                workflowId,
                null,
                false,
                actor.toString()
        );
    }


    @Test
    void activateRecordsActorEvidenceAfterAcademicScopeGuard() {

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID actor = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();

        UUID academicYearId = UUID.randomUUID();
        UUID academicTermId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();

        Timetable timetable =
                timetable(
                        tenantId,
                        timetableId,
                        workflowId
                );

        when(
                timetable.getAcademicYearId()
        ).thenReturn(
                academicYearId
        );

        when(
                timetable.getAcademicTermId()
        ).thenReturn(
                academicTermId
        );

        when(
                timetable.getCampusId()
        ).thenReturn(
                campusId
        );

        when(
                timetable.getTimetableType()
        ).thenReturn(
                "MASTER"
        );

        when(
                repository
                        .findFirstByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndTimetableStatusAndStatus(
                                tenantId,
                                academicYearId,
                                academicTermId,
                                campusId,
                                "MASTER",
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );

        service.activate(
                tenantId,
                timetableId,
                actor
        );

        verify(
                timetable
        ).activate();

        verify(
                history
        ).record(
                tenantId,
                timetableId,
                null,
                "TIMETABLE_ACTIVATED",
                "Published timetable activated for academic operations",
                actor,
                workflowId,
                null,
                false,
                actor.toString()
        );
    }


    @Test
    void suspendRecordsHumanEvidence() {

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID actor = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();

        Timetable timetable =
                timetable(
                        tenantId,
                        timetableId,
                        workflowId
                );

        service.suspend(
                tenantId,
                timetableId,
                actor
        );

        verify(
                timetable
        ).suspend();

        verify(
                history
        ).record(
                tenantId,
                timetableId,
                null,
                "TIMETABLE_SUSPENDED",
                "Active timetable suspended",
                actor,
                workflowId,
                null,
                false,
                actor.toString()
        );
    }


    @Test
    void rejectsMissingHumanActorBeforeChangingTimetable() {

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.submitForReview(
                                tenantId,
                                timetableId,
                                null
                        )
        );

        verifyNoInteractions(
                repository,
                history
        );
    }


    private Timetable timetable(
            UUID tenantId,
            UUID timetableId,
            UUID workflowId
    ) {

        Timetable timetable =
                mock(Timetable.class);

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(
                        timetable
                )
        );

        when(
                repository.save(
                        timetable
                )
        ).thenReturn(
                timetable
        );

        when(
                timetable.getWorkflowInstanceId()
        ).thenReturn(
                workflowId
        );

        return timetable;
    }
}
