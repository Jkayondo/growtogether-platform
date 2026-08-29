package africa.growtogether.platform.school.timetable.entry;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;

import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityConflict;
import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityEvaluator;

import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;

import africa.growtogether.platform.school.timetable.resource.SchedulingResource;
import africa.growtogether.platform.school.timetable.resource.SchedulingResourceRepository;

import africa.growtogether.platform.school.timetable.reliability.TimetableConflictService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TimetableEntryService {

    private static final Set<String> BLOCKING_ENTRY_STATUSES =
            Set.of(
                    "SCHEDULED",
                    "CONFIRMED",
                    "ACTIVE"
            );

    private static final Set<String> EDITABLE_TIMETABLE_STATUSES =
            Set.of(
                    "DRAFT",
                    "GENERATED",
                    "UNDER_REVIEW"
            );

    private final TimetableEntryRepository repository;
    private final TimetableRepository timetables;
    private final BellPeriodRepository bellPeriods;
    private final BellScheduleRepository bellSchedules;

    private final ClassOfferingRepository classOfferings;
    private final SubjectOfferingRepository subjectOfferings;
    private final ClassGradeRepository classGrades;
    private final StreamRepository streams;

    private final TeachingAssignmentRepository teachingAssignments;
    private final TeacherProfileRepository teachers;

    private final SchedulingResourceRepository resources;
    private final TimetableConflictService conflicts;
    private final TimetableAvailabilityEvaluator availability;

    public TimetableEntryService(
            TimetableEntryRepository repository,
            TimetableRepository timetables,
            BellPeriodRepository bellPeriods,
            BellScheduleRepository bellSchedules,
            ClassOfferingRepository classOfferings,
            SubjectOfferingRepository subjectOfferings,
            ClassGradeRepository classGrades,
            StreamRepository streams,
            TeachingAssignmentRepository teachingAssignments,
            TeacherProfileRepository teachers,
            SchedulingResourceRepository resources,
            TimetableConflictService conflicts,
            TimetableAvailabilityEvaluator availability
    ) {

        this.repository = repository;
        this.timetables = timetables;
        this.bellPeriods = bellPeriods;
        this.bellSchedules = bellSchedules;

        this.classOfferings = classOfferings;
        this.subjectOfferings = subjectOfferings;
        this.classGrades = classGrades;
        this.streams = streams;

        this.teachingAssignments = teachingAssignments;
        this.teachers = teachers;

        this.resources = resources;
        this.conflicts = conflicts;
        this.availability = availability;
    }

    @Transactional
    public TimetableEntry create(
            UUID tenantId,
            CreateTimetableEntryCommand command
    ) {

        Timetable timetable =
                timetables
                        .findByTenantIdAndId(
                                tenantId,
                                command.timetableId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Timetable not found for tenant"
                                )
                        );

        if (
                !EDITABLE_TIMETABLE_STATUSES.contains(
                        timetable.getTimetableStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Timetable entries cannot be added while timetable status is "
                            + timetable.getTimetableStatus()
            );
        }

        BellPeriod bellPeriod =
                bellPeriods
                        .findByTenantIdAndId(
                                tenantId,
                                command.bellPeriodId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Bell period not found for tenant"
                                )
                        );

        if (
                !timetable.getBellScheduleId().equals(
                        bellPeriod.getBellScheduleId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Bell period does not belong to timetable bell schedule"
            );
        }

        if (!bellPeriod.isSchedulingAllowed()) {
            throw new IllegalArgumentException(
                    "Scheduling is not allowed in bell period "
                            + bellPeriod.getPeriodCode()
            );
        }

        BellSchedule bellSchedule =
                bellSchedules
                        .findByTenantIdAndId(
                                tenantId,
                                timetable.getBellScheduleId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Bell schedule not found for tenant"
                                )
                        );

        String day =
                command.dayOfWeek()
                        .trim()
                        .toUpperCase();

        if (!isDayEnabled(bellSchedule, day)) {
            throw new IllegalArgumentException(
                    day + " is not enabled on timetable bell schedule"
            );
        }

        validateEntryDates(
                timetable,
                command.effectiveFrom(),
                command.effectiveTo()
        );

        ClassOffering classOffering = null;
        SubjectOffering subjectOffering = null;
        TeachingAssignment teachingAssignment = null;
        TeacherProfile teacher = null;

        if ("LESSON".equalsIgnoreCase(command.entryType())) {

            classOffering =
                    validateClassOffering(
                            tenantId,
                            timetable,
                            command
                    );

            subjectOffering =
                    validateSubjectOffering(
                            tenantId,
                            timetable,
                            classOffering,
                            command
                    );

            validateClassAndStream(
                    tenantId,
                    timetable,
                    classOffering,
                    subjectOffering,
                    command
            );

            teachingAssignment =
                    validateTeachingAssignment(
                            tenantId,
                            timetable,
                            subjectOffering,
                            command
                    );

            teacher =
                    validateTeacher(
                            tenantId,
                            teachingAssignment,
                            command.teacherProfileId()
                    );
        }

        SchedulingResource resource = null;

        if (command.schedulingResourceId() != null) {

            resource =
                    validateResource(
                            tenantId,
                            timetable,
                            subjectOffering,
                            command.schedulingResourceId()
                    );
        }

        TimetableEntry candidate =
                new TimetableEntry(
                        command.timetableId(),
                        command.bellPeriodId(),
                        command.dayOfWeek(),
                        command.classOfferingId(),
                        command.subjectOfferingId(),
                        command.classGradeId(),
                        command.streamId(),
                        command.teachingAssignmentId(),
                        command.teacherProfileId(),
                        command.schedulingResourceId(),
                        command.entryType(),
                        command.activityName(),
                        command.notes(),
                        command.recurring(),
                        command.recurrenceRule(),
                        command.effectiveFrom(),
                        command.effectiveTo()
                );

        /*
         * IMPROVEMENT:
         * Translate the school-local Bell Period into real-world
         * Instants and verify teacher/resource availability across
         * every occurrence of this timetable entry.
         */
        TimetableAvailabilityConflict availabilityConflict =
                availability
                        .evaluate(
                                tenantId,
                                timetable,
                                bellPeriod,
                                command
                        )
                        .orElse(null);

        if (availabilityConflict != null) {

            conflicts.recordDetectedConflict(
                    tenantId,
                    command.timetableId(),
                    null,
                    null,
                    availabilityConflict.conflictType(),
                    "ERROR",
                    availabilityConflict.description(),
                    "SYSTEM"
            );

            throw new IllegalArgumentException(
                    availabilityConflict.description()
            );
        }

        detectClassConflict(
                tenantId,
                candidate,
                classOffering,
                bellPeriod
        );

        detectTeacherConflict(
                tenantId,
                candidate,
                teacher,
                bellPeriod
        );

        detectResourceConflict(
                tenantId,
                candidate,
                resource,
                bellPeriod
        );

        candidate.setTenantId(
                tenantId
        );

        return repository.save(
                candidate
        );
    }

    private ClassOffering validateClassOffering(
            UUID tenantId,
            Timetable timetable,
            CreateTimetableEntryCommand command
    ) {

        ClassOffering offering =
                classOfferings
                        .findByTenantIdAndId(
                                tenantId,
                                command.classOfferingId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Class offering not found for tenant"
                                )
                        );

        if (
                !timetable.getAcademicYearId().equals(
                        offering.getAcademicYearId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Class offering does not belong to timetable academic year"
            );
        }

        if (
                !timetable.getCampusId().equals(
                        offering.getCampusId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Class offering does not belong to timetable campus"
            );
        }

        if (
                !command.classGradeId().equals(
                        offering.getClassGradeId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Class grade does not match class offering"
            );
        }

        return offering;
    }

    private SubjectOffering validateSubjectOffering(
            UUID tenantId,
            Timetable timetable,
            ClassOffering classOffering,
            CreateTimetableEntryCommand command
    ) {

        SubjectOffering offering =
                subjectOfferings
                        .findByTenantIdAndId(
                                tenantId,
                                command.subjectOfferingId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Subject offering not found for tenant"
                                )
                        );

        if (
                !command.classOfferingId().equals(
                        offering.getClassOfferingId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Subject offering does not belong to class offering"
            );
        }

        if (
                timetable.getAcademicTermId() != null
                && offering.getAcademicTermId() != null
                && !timetable.getAcademicTermId().equals(
                        offering.getAcademicTermId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Subject offering does not belong to timetable academic term"
            );
        }

        if (
                offering.getStreamId() != null
                && !offering.getStreamId().equals(
                        command.streamId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Subject offering stream does not match timetable entry stream"
            );
        }

        return offering;
    }

    private void validateClassAndStream(
            UUID tenantId,
            Timetable timetable,
            ClassOffering classOffering,
            SubjectOffering subjectOffering,
            CreateTimetableEntryCommand command
    ) {

        classGrades
                .findByTenantIdAndId(
                        tenantId,
                        command.classGradeId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found for tenant"
                        )
                );

        if (command.streamId() == null) {
            return;
        }

        Stream stream =
                streams
                        .findByTenantIdAndId(
                                tenantId,
                                command.streamId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Stream not found for tenant"
                                )
                        );

        if (
                !timetable.getCampusId().equals(
                        stream.getCampusId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Stream does not belong to timetable campus"
            );
        }

        if (
                !classOffering.getClassGradeId().equals(
                        stream.getClassGradeId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Stream does not belong to class grade"
            );
        }
    }

    private TeachingAssignment validateTeachingAssignment(
            UUID tenantId,
            Timetable timetable,
            SubjectOffering subjectOffering,
            CreateTimetableEntryCommand command
    ) {

        TeachingAssignment assignment =
                teachingAssignments
                        .findByTenantIdAndId(
                                tenantId,
                                command.teachingAssignmentId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Teaching assignment not found for tenant"
                                )
                        );

        if (
                !"ACTIVE".equals(
                        assignment.getAssignmentStatus()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment is not active"
            );
        }

        if (
                !timetable.getAcademicYearId().equals(
                        assignment.getAcademicYearId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment does not match timetable academic year"
            );
        }

        if (
                timetable.getAcademicTermId() != null
                && assignment.getAcademicTermId() != null
                && !timetable.getAcademicTermId().equals(
                        assignment.getAcademicTermId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment does not match timetable academic term"
            );
        }

        if (
                !timetable.getCampusId().equals(
                        assignment.getCampusId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment does not match timetable campus"
            );
        }

        if (
                !command.classGradeId().equals(
                        assignment.getClassGradeId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment does not match class grade"
            );
        }

        if (
                assignment.getStreamId() != null
                && !assignment.getStreamId().equals(
                        command.streamId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment does not match stream"
            );
        }

        if (
                !subjectOffering.getSubjectId().equals(
                        assignment.getSubjectId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teaching assignment does not match subject"
            );
        }

        if (
                !command.teacherProfileId().equals(
                        assignment.getTeacherProfileId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teacher does not match teaching assignment"
            );
        }

        LocalDate effectiveFrom =
                command.effectiveFrom() == null
                        ? timetable.getEffectiveFrom()
                        : command.effectiveFrom();

        LocalDate effectiveTo =
                command.effectiveTo() == null
                        ? timetable.getEffectiveTo()
                        : command.effectiveTo();

        if (
                effectiveFrom != null
                && assignment.getEffectiveFrom() != null
                && effectiveFrom.isBefore(
                        assignment.getEffectiveFrom()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable entry begins before teaching assignment"
            );
        }

        if (
                effectiveTo != null
                && assignment.getEffectiveTo() != null
                && effectiveTo.isAfter(
                        assignment.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable entry ends after teaching assignment"
            );
        }

        return assignment;
    }

    private TeacherProfile validateTeacher(
            UUID tenantId,
            TeachingAssignment assignment,
            UUID teacherProfileId
    ) {

        TeacherProfile teacher =
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

        if (
                !assignment.getTeacherProfileId().equals(
                        teacherProfileId
                )
        ) {
            throw new IllegalArgumentException(
                    "Teacher profile does not match teaching assignment"
            );
        }

        if (
                !"ACTIVE".equals(
                        teacher.getTeachingStatus()
                )
        ) {
            throw new IllegalArgumentException(
                    "Teacher is not active"
            );
        }

        return teacher;
    }

    private SchedulingResource validateResource(
            UUID tenantId,
            Timetable timetable,
            SubjectOffering subjectOffering,
            UUID resourceId
    ) {

        SchedulingResource resource =
                resources
                        .findByTenantIdAndId(
                                tenantId,
                                resourceId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Scheduling resource not found for tenant"
                                )
                        );

        if (
                !timetable.getCampusId().equals(
                        resource.getCampusId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Scheduling resource does not belong to timetable campus"
            );
        }

        if (!resource.isBookable()) {
            throw new IllegalArgumentException(
                    "Scheduling resource is not bookable"
            );
        }

        if (
                !"ACTIVE".equals(
                        resource.getResourceStatus()
                )
        ) {
            throw new IllegalArgumentException(
                    "Scheduling resource is not available"
            );
        }

        if (
                subjectOffering != null
                && resource.getSpecializedForSubjectId() != null
                && !resource.getSpecializedForSubjectId().equals(
                        subjectOffering.getSubjectId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Scheduling resource specialization does not match subject"
            );
        }

        return resource;
    }

    private void detectClassConflict(
            UUID tenantId,
            TimetableEntry candidate,
            ClassOffering classOffering,
            BellPeriod period
    ) {

        if (candidate.getClassGradeId() == null) {
            return;
        }

        List<TimetableEntry> existingEntries =
                repository
                        .findByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndClassGradeIdAndEntryStatusInAndStatus(
                                tenantId,
                                candidate.getTimetableId(),
                                candidate.getDayOfWeek(),
                                candidate.getBellPeriodId(),
                                candidate.getClassGradeId(),
                                BLOCKING_ENTRY_STATUSES,
                                EntityStatus.ACTIVE
                        );

        for (TimetableEntry existing : existingEntries) {

            boolean sameSchedulingScope =
                    candidate.getStreamId() == null
                    || existing.getStreamId() == null
                    || candidate.getStreamId().equals(
                            existing.getStreamId()
                    );

            if (sameSchedulingScope) {

                String classLabel =
                        classOffering == null
                                ? "Class"
                                : classOffering.getOfferingCode();

                String message =
                        classLabel
                                + " is already scheduled for "
                                + candidate.getDayOfWeek()
                                + " "
                                + period.getPeriodCode();

                conflicts.recordDetectedConflict(
                        tenantId,
                        candidate.getTimetableId(),
                        null,
                        existing.getId(),
                        "CLASS_DOUBLE_BOOKING",
                        "ERROR",
                        message,
                        "SYSTEM"
                );

                throw new IllegalArgumentException(
                        message
                );
            }
        }
    }

    private void detectTeacherConflict(
            UUID tenantId,
            TimetableEntry candidate,
            TeacherProfile teacher,
            BellPeriod period
    ) {

        if (candidate.getTeacherProfileId() == null) {
            return;
        }

        repository
                .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndTeacherProfileIdAndEntryStatusInAndStatus(
                        tenantId,
                        candidate.getTimetableId(),
                        candidate.getDayOfWeek(),
                        candidate.getBellPeriodId(),
                        candidate.getTeacherProfileId(),
                        BLOCKING_ENTRY_STATUSES,
                        EntityStatus.ACTIVE
                )
                .ifPresent(
                        existing -> {

                            String teacherLabel =
                                    teacher == null
                                            ? "Teacher"
                                            : "Teacher "
                                            + teacher.getTeacherNumber();

                            String message =
                                    teacherLabel
                                            + " is already scheduled for "
                                            + candidate.getDayOfWeek()
                                            + " "
                                            + period.getPeriodCode();

                            conflicts.recordDetectedConflict(
                                    tenantId,
                                    candidate.getTimetableId(),
                                    null,
                                    existing.getId(),
                                    "TEACHER_DOUBLE_BOOKING",
                                    "ERROR",
                                    message,
                                    "SYSTEM"
                            );

                            throw new IllegalArgumentException(
                                    message
                            );
                        }
                );
    }

    private void detectResourceConflict(
            UUID tenantId,
            TimetableEntry candidate,
            SchedulingResource resource,
            BellPeriod period
    ) {

        if (candidate.getSchedulingResourceId() == null) {
            return;
        }

        repository
                .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndSchedulingResourceIdAndEntryStatusInAndStatus(
                        tenantId,
                        candidate.getTimetableId(),
                        candidate.getDayOfWeek(),
                        candidate.getBellPeriodId(),
                        candidate.getSchedulingResourceId(),
                        BLOCKING_ENTRY_STATUSES,
                        EntityStatus.ACTIVE
                )
                .ifPresent(
                        existing -> {

                            String resourceLabel =
                                    resource == null
                                            ? "Resource"
                                            : resource.getResourceName();

                            String message =
                                    resourceLabel
                                            + " is already booked for "
                                            + candidate.getDayOfWeek()
                                            + " "
                                            + period.getPeriodCode();

                            /*
                             * V039 currently defines ROOM_DOUBLE_BOOKING
                             * rather than generic RESOURCE_DOUBLE_BOOKING.
                             * Non-room equipment therefore falls back to
                             * OTHER instead of being misclassified.
                             */
                            String conflictType =
                                    resource != null
                                    && "EQUIPMENT".equals(
                                            resource.getResourceType()
                                    )
                                            ? "OTHER"
                                            : "ROOM_DOUBLE_BOOKING";

                            conflicts.recordDetectedConflict(
                                    tenantId,
                                    candidate.getTimetableId(),
                                    null,
                                    existing.getId(),
                                    conflictType,
                                    "ERROR",
                                    message,
                                    "SYSTEM"
                            );

                            throw new IllegalArgumentException(
                                    message
                            );
                        }
                );
    }

    private void validateEntryDates(
            Timetable timetable,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                effectiveFrom != null
                && effectiveFrom.isBefore(
                        timetable.getEffectiveFrom()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable entry begins before timetable"
            );
        }

        if (
                timetable.getEffectiveTo() != null
                && effectiveFrom != null
                && effectiveFrom.isAfter(
                        timetable.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable entry begins after timetable"
            );
        }

        if (
                timetable.getEffectiveTo() != null
                && effectiveTo != null
                && effectiveTo.isAfter(
                        timetable.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable entry ends after timetable"
            );
        }
    }

    private boolean isDayEnabled(
            BellSchedule schedule,
            String day
    ) {

        return switch (day) {
            case "MONDAY" ->
                    schedule.isMondayEnabled();

            case "TUESDAY" ->
                    schedule.isTuesdayEnabled();

            case "WEDNESDAY" ->
                    schedule.isWednesdayEnabled();

            case "THURSDAY" ->
                    schedule.isThursdayEnabled();

            case "FRIDAY" ->
                    schedule.isFridayEnabled();

            case "SATURDAY" ->
                    schedule.isSaturdayEnabled();

            case "SUNDAY" ->
                    schedule.isSundayEnabled();

            default ->
                    throw new IllegalArgumentException(
                            "Invalid day of week: "
                                    + day
                    );
        };
    }

    @Transactional(readOnly = true)
    public TimetableEntry get(
            UUID tenantId,
            UUID entryId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        entryId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Timetable entry not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> findByTimetable(
            UUID tenantId,
            UUID timetableId
    ) {

        return repository
                .findByTenantIdAndTimetableId(
                        tenantId,
                        timetableId
                );
    }
}
