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
@Table(name = "gts_resource_unavailability")
public class ResourceUnavailability
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_REASON_TYPES =
            Set.of(
                    "MAINTENANCE",
                    "REPAIR",
                    "RESERVED",
                    "SAFETY",
                    "EVENT",
                    "CAPACITY_RESTRICTION",
                    "EMERGENCY",
                    "OTHER"
            );

    @Column(
            name = "scheduling_resource_id",
            nullable = false
    )
    private UUID schedulingResourceId;

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
            name = "workflow_instance_id"
    )
    private UUID workflowInstanceId;

    protected ResourceUnavailability() {
    }

    public ResourceUnavailability(
            UUID schedulingResourceId,
            Instant unavailableFrom,
            Instant unavailableTo,
            String reasonType,
            String reason,
            Boolean recurring,
            String recurrenceRule,
            UUID workflowInstanceId
    ) {

        if (schedulingResourceId == null) {
            throw new IllegalArgumentException(
                    "schedulingResourceId must not be null"
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
                    "Invalid resource unavailability reason type: "
                            + normalizedReasonType
            );
        }

        this.schedulingResourceId =
                schedulingResourceId;

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

        this.workflowInstanceId =
                workflowInstanceId;
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

    public UUID getSchedulingResourceId() {
        return schedulingResourceId;
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

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }
}
