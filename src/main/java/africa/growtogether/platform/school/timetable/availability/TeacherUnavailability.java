package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_teacher_unavailability")
public class TeacherUnavailability
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_REASON_TYPES =
            Set.of(
                    "LEAVE",
                    "MEETING",
                    "TRAINING",
                    "EXAMINATION_DUTY",
                    "OFFICIAL_DUTY",
                    "MEDICAL",
                    "RESTRICTION",
                    "OTHER"
            );

    @Column(
            name = "teacher_profile_id",
            nullable = false
    )
    private UUID teacherProfileId;

    @Column(
            name = "unavailable_from",
            nullable = false
    )
    private Instant unavailableFrom;

    @Column(
            name = "unavailable_to",
            nullable = false
    )
    private Instant unavailableTo;

    @Column(
            name = "reason_type",
            nullable = false,
            length = 30
    )
    private String reasonType;

    @Column(
            length = 1000
    )
    private String reason;

    @Column(
            nullable = false
    )
    private boolean recurring = false;

    @Column(
            name = "recurrence_rule",
            length = 500
    )
    private String recurrenceRule;

    @Column(
            name = "leave_extension_id"
    )
    private UUID leaveExtensionId;

    protected TeacherUnavailability() {
    }

    public TeacherUnavailability(
            UUID teacherProfileId,
            Instant unavailableFrom,
            Instant unavailableTo,
            String reasonType,
            String reason,
            Boolean recurring,
            String recurrenceRule,
            UUID leaveExtensionId
    ) {

        if (teacherProfileId == null) {
            throw new IllegalArgumentException(
                    "teacherProfileId must not be null"
            );
        }

        if (
                unavailableFrom == null
                || unavailableTo == null
        ) {
            throw new IllegalArgumentException(
                    "unavailableFrom and unavailableTo must not be null"
            );
        }

        if (!unavailableTo.isAfter(unavailableFrom)) {
            throw new IllegalArgumentException(
                    "unavailableTo must be after unavailableFrom"
            );
        }

        String normalizedReasonType =
                requireText(
                        reasonType,
                        "reasonType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_REASON_TYPES.contains(
                        normalizedReasonType
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid teacher unavailability reason type: "
                            + normalizedReasonType
            );
        }

        this.teacherProfileId =
                teacherProfileId;

        this.unavailableFrom =
                unavailableFrom;

        this.unavailableTo =
                unavailableTo;

        this.reasonType =
                normalizedReasonType;

        this.reason =
                reason;

        this.recurring =
                recurring != null
                        && recurring;

        this.recurrenceRule =
                recurrenceRule;

        this.leaveExtensionId =
                leaveExtensionId;
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

    public UUID getTeacherProfileId() {
        return teacherProfileId;
    }

    public Instant getUnavailableFrom() {
        return unavailableFrom;
    }

    public Instant getUnavailableTo() {
        return unavailableTo;
    }

    public String getReasonType() {
        return reasonType;
    }

    public String getReason() {
        return reason;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public UUID getLeaveExtensionId() {
        return leaveExtensionId;
    }
}
