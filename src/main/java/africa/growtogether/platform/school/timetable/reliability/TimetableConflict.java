package africa.growtogether.platform.school.timetable.reliability;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_timetable_conflict")
public class TimetableConflict
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "TEACHER_DOUBLE_BOOKING",
                    "CLASS_DOUBLE_BOOKING",
                    "ROOM_DOUBLE_BOOKING",
                    "RESOURCE_UNAVAILABLE",
                    "TEACHER_UNAVAILABLE",
                    "CAPACITY_EXCEEDED",
                    "SUBJECT_PERIOD_LIMIT",
                    "WORKLOAD_EXCEEDED",
                    "OUTSIDE_BELL_SCHEDULE",
                    "CURRICULUM_MISMATCH",
                    "OTHER"
            );

    private static final Set<String> ALLOWED_SEVERITIES =
            Set.of(
                    "INFO",
                    "WARNING",
                    "ERROR",
                    "CRITICAL"
            );

    private static final Set<String> ALLOWED_DETECTORS =
            Set.of(
                    "SYSTEM",
                    "RULE_ENGINE",
                    "AI_ASSISTED",
                    "MANUAL"
            );

    @Column(name = "timetable_id", nullable = false)
    private UUID timetableId;

    @Column(name = "timetable_entry_id")
    private UUID timetableEntryId;

    @Column(name = "conflicting_entry_id")
    private UUID conflictingEntryId;

    @Column(name = "conflict_type", nullable = false, length = 40)
    private String conflictType;

    @Column(name = "conflict_severity", nullable = false, length = 20)
    private String conflictSeverity;

    @Column(name = "conflict_description", nullable = false, length = 1500)
    private String conflictDescription;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "detected_by", nullable = false, length = 30)
    private String detectedBy;

    @Column(nullable = false)
    private boolean resolved = false;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolution_notes", length = 1500)
    private String resolutionNotes;

    protected TimetableConflict() {
    }

    public TimetableConflict(
            UUID timetableId,
            UUID timetableEntryId,
            UUID conflictingEntryId,
            String conflictType,
            String conflictSeverity,
            String conflictDescription,
            String detectedBy
    ) {

        if (timetableId == null) {
            throw new IllegalArgumentException(
                    "timetableId must not be null"
            );
        }

        String normalizedType =
                normalize(
                        conflictType,
                        "conflictType"
                );

        if (!ALLOWED_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException(
                    "Invalid conflict type: "
                            + normalizedType
            );
        }

        String normalizedSeverity =
                conflictSeverity == null
                        || conflictSeverity.isBlank()
                        ? "ERROR"
                        : conflictSeverity.trim()
                                .toUpperCase(Locale.ROOT);

        if (
                !ALLOWED_SEVERITIES.contains(
                        normalizedSeverity
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid conflict severity: "
                            + normalizedSeverity
            );
        }

        String normalizedDetector =
                detectedBy == null
                        || detectedBy.isBlank()
                        ? "SYSTEM"
                        : detectedBy.trim()
                                .toUpperCase(Locale.ROOT);

        if (
                !ALLOWED_DETECTORS.contains(
                        normalizedDetector
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid conflict detector: "
                            + normalizedDetector
            );
        }

        this.timetableId = timetableId;
        this.timetableEntryId = timetableEntryId;
        this.conflictingEntryId = conflictingEntryId;
        this.conflictType = normalizedType;
        this.conflictSeverity = normalizedSeverity;

        this.conflictDescription =
                requireText(
                        conflictDescription,
                        "conflictDescription"
                );

        this.detectedAt = Instant.now();
        this.detectedBy = normalizedDetector;
    }

    public void resolve(
            UUID resolvedBy,
            String resolutionNotes
    ) {

        if (resolvedBy == null) {
            throw new IllegalArgumentException(
                    "resolvedBy must not be null"
            );
        }

        if (resolved) {
            throw new IllegalStateException(
                    "Conflict is already resolved"
            );
        }

        this.resolved = true;
        this.resolvedAt = Instant.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = resolutionNotes;
    }

    private String normalize(
            String value,
            String field
    ) {

        return requireText(
                value,
                field
        ).toUpperCase(
                Locale.ROOT
        );
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

    public UUID getTimetableId() {
        return timetableId;
    }

    public UUID getTimetableEntryId() {
        return timetableEntryId;
    }

    public UUID getConflictingEntryId() {
        return conflictingEntryId;
    }

    public String getConflictType() {
        return conflictType;
    }

    public String getConflictSeverity() {
        return conflictSeverity;
    }

    public String getConflictDescription() {
        return conflictDescription;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public String getDetectedBy() {
        return detectedBy;
    }

    public boolean isResolved() {
        return resolved;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }
}
