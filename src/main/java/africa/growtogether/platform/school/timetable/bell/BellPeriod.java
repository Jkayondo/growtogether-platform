package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_bell_period")
public class BellPeriod
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_PERIOD_TYPES =
            Set.of(
                    "TEACHING",
                    "BREAK",
                    "LUNCH",
                    "ASSEMBLY",
                    "REGISTRATION",
                    "STUDY",
                    "SPORTS",
                    "CLUB",
                    "WORSHIP",
                    "BOARDING",
                    "EXAMINATION",
                    "OTHER"
            );

    @Column(name = "bell_schedule_id", nullable = false)
    private UUID bellScheduleId;

    @Column(name = "period_code", nullable = false, length = 60)
    private String periodCode;

    @Column(name = "period_name", nullable = false, length = 160)
    private String periodName;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "period_type", nullable = false, length = 30)
    private String periodType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "instructional_minutes")
    private Integer instructionalMinutes;

    @Column(name = "attendance_required", nullable = false)
    private boolean attendanceRequired;

    @Column(name = "scheduling_allowed", nullable = false)
    private boolean schedulingAllowed;

    protected BellPeriod() {
    }

    public BellPeriod(
            UUID bellScheduleId,
            String periodCode,
            String periodName,
            Integer sequenceNumber,
            String periodType,
            LocalTime startTime,
            LocalTime endTime,
            Integer instructionalMinutes,
            Boolean attendanceRequired,
            Boolean schedulingAllowed
    ) {

        if (bellScheduleId == null) {
            throw new IllegalArgumentException(
                    "bellScheduleId must not be null"
            );
        }

        if (
                sequenceNumber == null
                || sequenceNumber <= 0
        ) {
            throw new IllegalArgumentException(
                    "sequenceNumber must be greater than zero"
            );
        }

        if (
                startTime == null
                || endTime == null
        ) {
            throw new IllegalArgumentException(
                    "startTime and endTime must not be null"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "endTime must be after startTime"
            );
        }

        String normalizedType =
                requireText(
                        periodType,
                        "periodType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_PERIOD_TYPES.contains(
                        normalizedType
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid period type: "
                            + normalizedType
            );
        }

        long actualDuration =
                Duration.between(
                        startTime,
                        endTime
                ).toMinutes();

        if (
                instructionalMinutes != null
                && instructionalMinutes < 0
        ) {
            throw new IllegalArgumentException(
                    "instructionalMinutes must not be negative"
            );
        }

        if (
                instructionalMinutes != null
                && instructionalMinutes > actualDuration
        ) {
            throw new IllegalArgumentException(
                    "instructionalMinutes must not exceed period duration"
            );
        }

        this.bellScheduleId = bellScheduleId;
        this.periodCode =
                requireText(
                        periodCode,
                        "periodCode"
                );
        this.periodName =
                requireText(
                        periodName,
                        "periodName"
                );
        this.sequenceNumber = sequenceNumber;
        this.periodType = normalizedType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.instructionalMinutes = instructionalMinutes;

        this.attendanceRequired =
                attendanceRequired == null
                        ? true
                        : attendanceRequired;

        this.schedulingAllowed =
                schedulingAllowed == null
                        ? true
                        : schedulingAllowed;
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

    public UUID getBellScheduleId() {
        return bellScheduleId;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public String getPeriodName() {
        return periodName;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public String getPeriodType() {
        return periodType;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Integer getInstructionalMinutes() {
        return instructionalMinutes;
    }

    public boolean isAttendanceRequired() {
        return attendanceRequired;
    }

    public boolean isSchedulingAllowed() {
        return schedulingAllowed;
    }
}
