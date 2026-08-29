package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_bell_schedule")
public class BellSchedule
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "REGULAR",
                    "EXAMINATION",
                    "SHORT_DAY",
                    "WEEKEND",
                    "BOARDING",
                    "HOLIDAY_PROGRAMME",
                    "EMERGENCY",
                    "OTHER"
            );

    @Column(name = "campus_id", nullable = false)
    private UUID campusId;

    @Column(name = "schedule_code", nullable = false, length = 80)
    private String scheduleCode;

    @Column(name = "schedule_name", nullable = false, length = 200)
    private String scheduleName;

    @Column(length = 1000)
    private String description;

    @Column(name = "schedule_type", nullable = false, length = 30)
    private String scheduleType;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "monday_enabled", nullable = false)
    private boolean mondayEnabled;

    @Column(name = "tuesday_enabled", nullable = false)
    private boolean tuesdayEnabled;

    @Column(name = "wednesday_enabled", nullable = false)
    private boolean wednesdayEnabled;

    @Column(name = "thursday_enabled", nullable = false)
    private boolean thursdayEnabled;

    @Column(name = "friday_enabled", nullable = false)
    private boolean fridayEnabled;

    @Column(name = "saturday_enabled", nullable = false)
    private boolean saturdayEnabled;

    @Column(name = "sunday_enabled", nullable = false)
    private boolean sundayEnabled;

    @Column(name = "schedule_status", nullable = false, length = 30)
    private String scheduleStatus = "DRAFT";

    protected BellSchedule() {
    }

    public BellSchedule(
            UUID campusId,
            String scheduleCode,
            String scheduleName,
            String description,
            String scheduleType,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Boolean mondayEnabled,
            Boolean tuesdayEnabled,
            Boolean wednesdayEnabled,
            Boolean thursdayEnabled,
            Boolean fridayEnabled,
            Boolean saturdayEnabled,
            Boolean sundayEnabled
    ) {

        if (campusId == null) {
            throw new IllegalArgumentException(
                    "campusId must not be null"
            );
        }

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        String normalizedType =
                requireText(
                        scheduleType,
                        "scheduleType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (!ALLOWED_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException(
                    "Invalid schedule type: "
                            + normalizedType
            );
        }

        this.mondayEnabled =
                mondayEnabled == null
                        ? true
                        : mondayEnabled;

        this.tuesdayEnabled =
                tuesdayEnabled == null
                        ? true
                        : tuesdayEnabled;

        this.wednesdayEnabled =
                wednesdayEnabled == null
                        ? true
                        : wednesdayEnabled;

        this.thursdayEnabled =
                thursdayEnabled == null
                        ? true
                        : thursdayEnabled;

        this.fridayEnabled =
                fridayEnabled == null
                        ? true
                        : fridayEnabled;

        this.saturdayEnabled =
                saturdayEnabled == null
                        ? false
                        : saturdayEnabled;

        this.sundayEnabled =
                sundayEnabled == null
                        ? false
                        : sundayEnabled;

        if (
                !this.mondayEnabled
                && !this.tuesdayEnabled
                && !this.wednesdayEnabled
                && !this.thursdayEnabled
                && !this.fridayEnabled
                && !this.saturdayEnabled
                && !this.sundayEnabled
        ) {
            throw new IllegalArgumentException(
                    "At least one schedule day must be enabled"
            );
        }

        this.campusId = campusId;
        this.scheduleCode =
                requireText(
                        scheduleCode,
                        "scheduleCode"
                );
        this.scheduleName =
                requireText(
                        scheduleName,
                        "scheduleName"
                );
        this.description = description;
        this.scheduleType = normalizedType;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public void approve() {
        this.scheduleStatus = "APPROVED";
    }

    public void activate() {
        this.scheduleStatus = "ACTIVE";
    }

    public void suspend() {
        this.scheduleStatus = "SUSPENDED";
    }

    public void supersede() {
        this.scheduleStatus = "SUPERSEDED";
    }

    public void archiveSchedule() {
        this.scheduleStatus = "ARCHIVED";
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

    public UUID getCampusId() {
        return campusId;
    }

    public String getScheduleCode() {
        return scheduleCode;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public boolean isMondayEnabled() {
        return mondayEnabled;
    }

    public boolean isTuesdayEnabled() {
        return tuesdayEnabled;
    }

    public boolean isWednesdayEnabled() {
        return wednesdayEnabled;
    }

    public boolean isThursdayEnabled() {
        return thursdayEnabled;
    }

    public boolean isFridayEnabled() {
        return fridayEnabled;
    }

    public boolean isSaturdayEnabled() {
        return saturdayEnabled;
    }

    public boolean isSundayEnabled() {
        return sundayEnabled;
    }

    public String getScheduleStatus() {
        return scheduleStatus;
    }
}
