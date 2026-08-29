package africa.growtogether.platform.school.timetable.core;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;
import africa.growtogether.platform.school.timetable.reliability.TimetableChangeHistoryService;

import africa.growtogether.platform.common.web.RequestContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TimetableService {

    private final TimetableRepository repository;
    private final AcademicYearRepository academicYears;
    private final AcademicTermRepository academicTerms;
    private final CampusRepository campuses;
    private final BellScheduleRepository bellSchedules;
    private final TimetableChangeHistoryService history;

    public TimetableService(
            TimetableRepository repository,
            AcademicYearRepository academicYears,
            AcademicTermRepository academicTerms,
            CampusRepository campuses,
            BellScheduleRepository bellSchedules,
            TimetableChangeHistoryService history
    ) {
        this.repository = repository;
        this.academicYears = academicYears;
        this.academicTerms = academicTerms;
        this.campuses = campuses;
        this.bellSchedules = bellSchedules;
        this.history = history;
    }

    @Transactional
    public Timetable create(
            UUID tenantId,
            CreateTimetableCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        academicYears
                .findByTenantIdAndId(
                        tenantId,
                        command.academicYearId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Academic year not found for tenant"
                        )
                );

        AcademicTerm term = null;

        if (command.academicTermId() != null) {

            term =
                    academicTerms
                            .findByTenantIdAndId(
                                    tenantId,
                                    command.academicTermId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Academic term not found for tenant"
                                    )
                            );

            if (
                    term.getAcademicYear() == null
                    || !command.academicYearId().equals(
                            term.getAcademicYear().getId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Academic term does not belong to academic year"
                );
            }

            validateTermEffectiveDates(
                    term,
                    command.effectiveFrom(),
                    command.effectiveTo()
            );
        }

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        command.campusId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        BellSchedule bellSchedule =
                bellSchedules
                        .findByTenantIdAndId(
                                tenantId,
                                command.bellScheduleId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Bell schedule not found for tenant"
                                )
                        );

        if (
                !command.campusId().equals(
                        bellSchedule.getCampusId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Bell schedule does not belong to campus"
            );
        }

        validateBellScheduleEffectiveDates(
                bellSchedule,
                command.effectiveFrom(),
                command.effectiveTo()
        );

        if (
                repository.existsByTenantIdAndTimetableCode(
                        tenantId,
                        command.timetableCode()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable code already exists for tenant"
            );
        }

        String timetableType =
                command.timetableType()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        Integer versionNumber =
                command.versionNumber() == null
                        ? 1
                        : command.versionNumber();

        if (
                repository
                        .existsByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndVersionNumber(
                                tenantId,
                                command.academicYearId(),
                                command.academicTermId(),
                                command.campusId(),
                                timetableType,
                                versionNumber
                        )
        ) {
            throw new IllegalArgumentException(
                    "Timetable version already exists for academic scope"
            );
        }

        Timetable timetable =
                new Timetable(
                        command.timetableCode(),
                        command.timetableName(),
                        command.description(),
                        command.academicYearId(),
                        command.academicTermId(),
                        command.campusId(),
                        command.bellScheduleId(),
                        timetableType,
                        versionNumber,
                        command.effectiveFrom(),
                        command.effectiveTo(),
                        command.generatedBy(),
                        command.generationReference(),
                        command.workflowInstanceId()
                );

        timetable.setTenantId(
                tenantId
        );

        return repository.save(
                timetable
        );
    }

    @Transactional(readOnly = true)
    public Timetable get(
            UUID tenantId,
            UUID timetableId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        timetableId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Timetable not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Timetable> findByAcademicYear(
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
    public List<Timetable> findByCampus(
            UUID tenantId,
            UUID campusId
    ) {

        return repository
                .findByTenantIdAndCampusId(
                        tenantId,
                        campusId
                );
    }

    @Transactional
    public Timetable submitForReview(
            UUID tenantId,
            UUID timetableId,
            UUID submittedBy
    ) {

        UUID actor =
                requireActor(
                        submittedBy,
                        "submittedBy"
                );

        Timetable timetable =
                get(
                        tenantId,
                        timetableId
                );

        timetable.submitForReview();

        Timetable saved =
                repository.save(
                        timetable
                );

        recordLifecycleHistory(
                tenantId,
                timetableId,
                timetable,
                "TIMETABLE_SUBMITTED_FOR_REVIEW",
                "Timetable submitted for human review",
                actor
        );

        return saved;
    }


    @Transactional
    public Timetable approve(
            UUID tenantId,
            UUID timetableId,
            UUID approvedBy
    ) {

        UUID actor =
                requireActor(
                        approvedBy,
                        "approvedBy"
                );

        Timetable timetable =
                get(
                        tenantId,
                        timetableId
                );

        timetable.approve(
                actor
        );

        Timetable saved =
                repository.save(
                        timetable
                );

        recordLifecycleHistory(
                tenantId,
                timetableId,
                timetable,
                "TIMETABLE_APPROVED",
                "Timetable approved by authorised human reviewer",
                actor
        );

        return saved;
    }


    @Transactional
    public Timetable publish(
            UUID tenantId,
            UUID timetableId,
            UUID publishedBy
    ) {

        UUID actor =
                requireActor(
                        publishedBy,
                        "publishedBy"
                );

        Timetable timetable =
                get(
                        tenantId,
                        timetableId
                );

        timetable.publish(
                actor
        );

        Timetable saved =
                repository.save(
                        timetable
                );

        recordLifecycleHistory(
                tenantId,
                timetableId,
                timetable,
                "TIMETABLE_PUBLISHED",
                "Approved timetable published",
                actor
        );

        return saved;
    }


    @Transactional
    public Timetable activate(
            UUID tenantId,
            UUID timetableId,
            UUID activatedBy
    ) {

        UUID actor =
                requireActor(
                        activatedBy,
                        "activatedBy"
                );

        Timetable timetable =
                get(
                        tenantId,
                        timetableId
                );

        repository
                .findFirstByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndTimetableStatusAndStatus(
                        tenantId,
                        timetable.getAcademicYearId(),
                        timetable.getAcademicTermId(),
                        timetable.getCampusId(),
                        timetable.getTimetableType(),
                        "ACTIVE",
                        EntityStatus.ACTIVE
                )
                .filter(
                        existing ->
                                !existing.getId().equals(
                                        timetable.getId()
                                )
                )
                .ifPresent(
                        existing -> {
                            throw new IllegalArgumentException(
                                    "Another active timetable already exists for academic scope"
                            );
                        }
                );

        timetable.activate();

        Timetable saved =
                repository.save(
                        timetable
                );

        recordLifecycleHistory(
                tenantId,
                timetableId,
                timetable,
                "TIMETABLE_ACTIVATED",
                "Published timetable activated for academic operations",
                actor
        );

        return saved;
    }


    @Transactional
    public Timetable suspend(
            UUID tenantId,
            UUID timetableId,
            UUID suspendedBy
    ) {

        UUID actor =
                requireActor(
                        suspendedBy,
                        "suspendedBy"
                );

        Timetable timetable =
                get(
                        tenantId,
                        timetableId
                );

        timetable.suspend();

        Timetable saved =
                repository.save(
                        timetable
                );

        recordLifecycleHistory(
                tenantId,
                timetableId,
                timetable,
                "TIMETABLE_SUSPENDED",
                "Active timetable suspended",
                actor
        );

        return saved;
    }


    private void recordLifecycleHistory(
            UUID tenantId,
            UUID timetableId,
            Timetable timetable,
            String changeType,
            String changeReason,
            UUID changedBy
    ) {

        String correlationId =
                RequestContextHolder
                        .current()
                        .map(
                                context ->
                                        context.correlationId()
                        )
                        .filter(
                                value ->
                                        !value.isBlank()
                        )
                        .orElse(
                                null
                        );

        history.record(
                tenantId,
                timetableId,
                null,
                changeType,
                changeReason,
                changedBy,
                timetable.getWorkflowInstanceId(),
                correlationId,
                false,
                changedBy.toString()
        );
    }


    private UUID requireActor(
            UUID actorId,
            String field
    ) {

        if (actorId == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }

        return actorId;
    }


    private void validateTermEffectiveDates(
            AcademicTerm term,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                effectiveFrom != null
                && effectiveFrom.isBefore(
                        term.getStartDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable effectiveFrom is before academic term"
            );
        }

        if (
                effectiveFrom != null
                && effectiveFrom.isAfter(
                        term.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable effectiveFrom is after academic term"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isAfter(
                        term.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable effectiveTo is after academic term"
            );
        }
    }

    private void validateBellScheduleEffectiveDates(
            BellSchedule schedule,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                effectiveFrom != null
                && effectiveFrom.isBefore(
                        schedule.getEffectiveFrom()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable effectiveFrom is before bell schedule"
            );
        }

        if (
                schedule.getEffectiveTo() != null
                && effectiveFrom != null
                && effectiveFrom.isAfter(
                        schedule.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable effectiveFrom is after bell schedule"
            );
        }

        if (
                schedule.getEffectiveTo() != null
                && effectiveTo != null
                && effectiveTo.isAfter(
                        schedule.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable effectiveTo is after bell schedule"
            );
        }
    }
}
