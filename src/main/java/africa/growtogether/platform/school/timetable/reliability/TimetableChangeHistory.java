package africa.growtogether.platform.school.timetable.reliability;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_timetable_change_history")
public class TimetableChangeHistory {

    private static final Set<String> ALLOWED_CHANGE_TYPES =
            Set.of(
                    "TIMETABLE_CREATED",
                    "TIMETABLE_GENERATED",
                    "TIMETABLE_SUBMITTED_FOR_REVIEW",
                    "TIMETABLE_APPROVED",
                    "TIMETABLE_PUBLISHED",
                    "TIMETABLE_ACTIVATED",
                    "TIMETABLE_SUSPENDED",
                    "ENTRY_ADDED",
                    "ENTRY_UPDATED",
                    "ENTRY_MOVED",
                    "ENTRY_CANCELLED",
                    "TEACHER_CHANGED",
                    "ROOM_CHANGED",
                    "PERIOD_CHANGED",
                    "CONFLICT_DETECTED",
                    "CONFLICT_RESOLVED",
                    "VERSION_SUPERSEDED",
                    "OTHER"
            );

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "timetable_id", nullable = false)
    private UUID timetableId;

    @Column(name = "timetable_entry_id")
    private UUID timetableEntryId;

    @Column(name = "change_type", nullable = false, length = 40)
    private String changeType;

    @Column(name = "change_reason", length = 1500)
    private String changeReason;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "correlation_id", length = 120)
    private String correlationId;

    @Column(name = "notification_required", nullable = false)
    private boolean notificationRequired;

    @Column(name = "notification_sent_at")
    private Instant notificationSentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 150)
    private String createdBy;

    protected TimetableChangeHistory() {
    }

    public TimetableChangeHistory(
            UUID tenantId,
            UUID timetableId,
            UUID timetableEntryId,
            String changeType,
            String changeReason,
            UUID changedBy,
            UUID workflowInstanceId,
            String correlationId,
            boolean notificationRequired,
            String createdBy
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (timetableId == null) {
            throw new IllegalArgumentException(
                    "timetableId must not be null"
            );
        }

        String normalizedType =
                requireText(
                        changeType,
                        "changeType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_CHANGE_TYPES.contains(
                        normalizedType
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid timetable change type: "
                            + normalizedType
            );
        }

        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.timetableId = timetableId;
        this.timetableEntryId = timetableEntryId;
        this.changeType = normalizedType;
        this.changeReason = changeReason;
        this.effectiveAt = Instant.now();
        this.changedBy = changedBy;
        this.workflowInstanceId = workflowInstanceId;
        this.correlationId = correlationId;
        this.notificationRequired = notificationRequired;
        this.createdAt = Instant.now();

        this.createdBy =
                createdBy == null
                        || createdBy.isBlank()
                        ? "system"
                        : createdBy.trim();
    }

    public void markNotificationSent() {
        this.notificationSentAt = Instant.now();
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

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getTimetableId() {
        return timetableId;
    }

    public UUID getTimetableEntryId() {
        return timetableEntryId;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public Instant getEffectiveAt() {
        return effectiveAt;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public boolean isNotificationRequired() {
        return notificationRequired;
    }

    public Instant getNotificationSentAt() {
        return notificationSentAt;
    }
}
