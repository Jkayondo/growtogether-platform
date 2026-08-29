package africa.growtogether.platform.school.timetable.entry;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_timetable_entry")
public class TimetableEntry extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_DAYS =
            Set.of(
                    "MONDAY",
                    "TUESDAY",
                    "WEDNESDAY",
                    "THURSDAY",
                    "FRIDAY",
                    "SATURDAY",
                    "SUNDAY"
            );

    private static final Set<String> ALLOWED_ENTRY_TYPES =
            Set.of(
                    "LESSON",
                    "BREAK",
                    "LUNCH",
                    "ASSEMBLY",
                    "STUDY",
                    "SPORTS",
                    "CLUB",
                    "WORSHIP",
                    "EXAMINATION",
                    "COUNSELLING",
                    "SPECIAL_SUPPORT",
                    "OTHER"
            );

    @Column(name = "timetable_id", nullable = false)
    private UUID timetableId;

    @Column(name = "bell_period_id", nullable = false)
    private UUID bellPeriodId;

    @Column(name = "day_of_week", nullable = false, length = 15)
    private String dayOfWeek;

    @Column(name = "class_offering_id")
    private UUID classOfferingId;

    @Column(name = "subject_offering_id")
    private UUID subjectOfferingId;

    @Column(name = "class_grade_id")
    private UUID classGradeId;

    @Column(name = "stream_id")
    private UUID streamId;

    @Column(name = "teaching_assignment_id")
    private UUID teachingAssignmentId;

    @Column(name = "teacher_profile_id")
    private UUID teacherProfileId;

    @Column(name = "scheduling_resource_id")
    private UUID schedulingResourceId;

    @Column(name = "entry_type", nullable = false, length = 30)
    private String entryType;

    @Column(name = "activity_name", length = 250)
    private String activityName;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private boolean recurring = true;

    @Column(name = "recurrence_rule", length = 500)
    private String recurrenceRule;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "entry_status", nullable = false, length = 30)
    private String entryStatus = "SCHEDULED";

    protected TimetableEntry() {
    }

    public TimetableEntry(
            UUID timetableId,
            UUID bellPeriodId,
            String dayOfWeek,
            UUID classOfferingId,
            UUID subjectOfferingId,
            UUID classGradeId,
            UUID streamId,
            UUID teachingAssignmentId,
            UUID teacherProfileId,
            UUID schedulingResourceId,
            String entryType,
            String activityName,
            String notes,
            Boolean recurring,
            String recurrenceRule,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (timetableId == null) {
            throw new IllegalArgumentException(
                    "timetableId must not be null"
            );
        }

        if (bellPeriodId == null) {
            throw new IllegalArgumentException(
                    "bellPeriodId must not be null"
            );
        }

        String normalizedDay =
                requireText(
                        dayOfWeek,
                        "dayOfWeek"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (!ALLOWED_DAYS.contains(normalizedDay)) {
            throw new IllegalArgumentException(
                    "Invalid day of week: "
                            + normalizedDay
            );
        }

        String normalizedType =
                requireText(
                        entryType,
                        "entryType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (!ALLOWED_ENTRY_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException(
                    "Invalid timetable entry type: "
                            + normalizedType
            );
        }

        if (
                effectiveFrom != null
                && effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        if ("LESSON".equals(normalizedType)) {

            requireId(
                    classOfferingId,
                    "classOfferingId"
            );

            requireId(
                    subjectOfferingId,
                    "subjectOfferingId"
            );

            requireId(
                    classGradeId,
                    "classGradeId"
            );

            requireId(
                    teachingAssignmentId,
                    "teachingAssignmentId"
            );

            requireId(
                    teacherProfileId,
                    "teacherProfileId"
            );
        }

        this.timetableId = timetableId;
        this.bellPeriodId = bellPeriodId;
        this.dayOfWeek = normalizedDay;

        this.classOfferingId = classOfferingId;
        this.subjectOfferingId = subjectOfferingId;
        this.classGradeId = classGradeId;
        this.streamId = streamId;

        this.teachingAssignmentId = teachingAssignmentId;
        this.teacherProfileId = teacherProfileId;
        this.schedulingResourceId = schedulingResourceId;

        this.entryType = normalizedType;
        this.activityName = activityName;
        this.notes = notes;

        this.recurring =
                recurring == null
                        ? true
                        : recurring;

        this.recurrenceRule = recurrenceRule;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public void confirm() {

        if (!"SCHEDULED".equals(entryStatus)) {
            throw new IllegalStateException(
                    "Only SCHEDULED entries can be confirmed"
            );
        }

        entryStatus = "CONFIRMED";
    }

    public void activate() {

        if (
                !"SCHEDULED".equals(entryStatus)
                && !"CONFIRMED".equals(entryStatus)
        ) {
            throw new IllegalStateException(
                    "Only SCHEDULED or CONFIRMED entries can be activated"
            );
        }

        entryStatus = "ACTIVE";
    }

    public void complete() {
        entryStatus = "COMPLETED";
    }

    public void cancel() {
        entryStatus = "CANCELLED";
    }

    public void markMoved() {
        entryStatus = "MOVED";
    }

    public void markReplaced() {
        entryStatus = "REPLACED";
    }

    private String requireText(
            String value,
            String field
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }

    private void requireId(
            UUID value,
            String field
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null for LESSON"
            );
        }
    }

    public UUID getTimetableId() {
        return timetableId;
    }

    public UUID getBellPeriodId() {
        return bellPeriodId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public UUID getClassOfferingId() {
        return classOfferingId;
    }

    public UUID getSubjectOfferingId() {
        return subjectOfferingId;
    }

    public UUID getClassGradeId() {
        return classGradeId;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public UUID getTeachingAssignmentId() {
        return teachingAssignmentId;
    }

    public UUID getTeacherProfileId() {
        return teacherProfileId;
    }

    public UUID getSchedulingResourceId() {
        return schedulingResourceId;
    }

    public String getEntryType() {
        return entryType;
    }

    public String getActivityName() {
        return activityName;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getEntryStatus() {
        return entryStatus;
    }
}
