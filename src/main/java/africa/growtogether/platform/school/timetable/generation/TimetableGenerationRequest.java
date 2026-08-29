package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "gts_timetable_generation_request"
)
public class TimetableGenerationRequest
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_MODES =
            Set.of(
                    "RULE_ENGINE",
                    "AI_ASSISTED"
            );

    @Column(
            name = "generation_code",
            nullable = false,
            length = 100
    )
    private String generationCode;

    @Column(
            name = "academic_year_id",
            nullable = false
    )
    private UUID academicYearId;

    @Column(
            name = "academic_term_id"
    )
    private UUID academicTermId;

    @Column(
            name = "campus_id",
            nullable = false
    )
    private UUID campusId;

    @Column(
            name = "bell_schedule_id",
            nullable = false
    )
    private UUID bellScheduleId;

    @Column(
            name = "timetable_type",
            nullable = false,
            length = 30
    )
    private String timetableType;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;

    @Column(
            name = "effective_to"
    )
    private LocalDate effectiveTo;

    @Column(
            name = "generation_mode",
            nullable = false,
            length = 30
    )
    private String generationMode;

    @Column(
            name = "model_code",
            length = 100
    )
    private String modelCode;

    @Column(
            name = "objectives",
            columnDefinition = "text"
    )
    private String objectives;

    @Column(
            name = "generation_status",
            nullable = false,
            length = 30
    )
    private String generationStatus =
            "DRAFT";

    @Column(
            name = "eaif_request_id"
    )
    private UUID eaifRequestId;

    @Column(
            name = "result_timetable_id"
    )
    private UUID resultTimetableId;

    @Column(
            name = "requested_by",
            nullable = false
    )
    private UUID requestedBy;

    @Column(
            name = "requested_at",
            nullable = false
    )
    private Instant requestedAt;

    @Column(
            name = "started_at"
    )
    private Instant startedAt;

    @Column(
            name = "completed_at"
    )
    private Instant completedAt;

    @Column(
            name = "failure_reason",
            columnDefinition = "text"
    )
    private String failureReason;

    protected TimetableGenerationRequest() {
    }

    public TimetableGenerationRequest(
            String generationCode,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID bellScheduleId,
            String timetableType,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String generationMode,
            String modelCode,
            String objectives,
            UUID requestedBy
    ) {

        if (academicYearId == null) {
            throw new IllegalArgumentException(
                    "academicYearId must not be null"
            );
        }

        if (campusId == null) {
            throw new IllegalArgumentException(
                    "campusId must not be null"
            );
        }

        if (bellScheduleId == null) {
            throw new IllegalArgumentException(
                    "bellScheduleId must not be null"
            );
        }

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isBefore(
                        effectiveFrom
                )
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        if (requestedBy == null) {
            throw new IllegalArgumentException(
                    "requestedBy must not be null"
            );
        }

        String normalizedMode =
                requireText(
                        generationMode,
                        "generationMode"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_MODES.contains(
                        normalizedMode
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid timetable generation mode: "
                            + normalizedMode
            );
        }

        String normalizedModel =
                normalizeNullable(
                        modelCode
                );

        if (
                "AI_ASSISTED".equals(
                        normalizedMode
                )
                && normalizedModel == null
        ) {
            throw new IllegalArgumentException(
                    "modelCode is required for AI-assisted generation"
            );
        }

        this.generationCode =
                requireText(
                        generationCode,
                        "generationCode"
                ).toUpperCase(
                        Locale.ROOT
                );

        this.academicYearId =
                academicYearId;

        this.academicTermId =
                academicTermId;

        this.campusId =
                campusId;

        this.bellScheduleId =
                bellScheduleId;

        this.timetableType =
                requireText(
                        timetableType,
                        "timetableType"
                ).toUpperCase(
                        Locale.ROOT
                );

        this.effectiveFrom =
                effectiveFrom;

        this.effectiveTo =
                effectiveTo;

        this.generationMode =
                normalizedMode;

        this.modelCode =
                normalizedModel == null
                        ? null
                        : normalizedModel.toUpperCase(
                                Locale.ROOT
                        );

        this.objectives =
                normalizeNullable(
                        objectives
                );

        this.requestedBy =
                requestedBy;

        this.requestedAt =
                Instant.now();
    }

    public void markReady() {

        requireStatus(
                "DRAFT"
        );

        this.generationStatus =
                "READY";
    }

    public void linkEaifRequest(
            UUID eaifRequestId
    ) {

        if (eaifRequestId == null) {
            throw new IllegalArgumentException(
                    "eaifRequestId must not be null"
            );
        }

        if (
                !"AI_ASSISTED".equals(
                        generationMode
                )
        ) {
            throw new IllegalStateException(
                    "EAIF request can only be linked to AI-assisted generation"
            );
        }

        this.eaifRequestId =
                eaifRequestId;
    }

    public void beginGeneration() {

        requireStatus(
                "READY"
        );

        this.generationStatus =
                "GENERATING";

        this.startedAt =
                Instant.now();

        this.failureReason =
                null;
    }

    public void markGenerated(
            UUID resultTimetableId
    ) {

        requireStatus(
                "GENERATING"
        );

        if (resultTimetableId == null) {
            throw new IllegalArgumentException(
                    "resultTimetableId must not be null"
            );
        }

        this.resultTimetableId =
                resultTimetableId;

        this.completedAt =
                Instant.now();

        this.generationStatus =
                "GENERATED";
    }

    public void fail(
            String reason
    ) {

        requireStatus(
                "GENERATING"
        );

        this.failureReason =
                requireText(
                        reason,
                        "reason"
                );

        this.completedAt =
                Instant.now();

        this.generationStatus =
                "FAILED";
    }

    public void cancel() {

        if (
                "GENERATED".equals(
                        generationStatus
                )
                || "FAILED".equals(
                        generationStatus
                )
                || "CANCELLED".equals(
                        generationStatus
                )
        ) {
            throw new IllegalStateException(
                    "Generation request cannot be cancelled from status "
                            + generationStatus
            );
        }

        this.completedAt =
                Instant.now();

        this.generationStatus =
                "CANCELLED";
    }

    private void requireStatus(
            String required
    ) {

        if (
                !required.equals(
                        generationStatus
                )
        ) {
            throw new IllegalStateException(
                    "Generation request must be "
                            + required
                            + " but is "
                            + generationStatus
            );
        }
    }

    private static String requireText(
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

    private static String normalizeNullable(
            String value
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    public String getGenerationCode() {
        return generationCode;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public UUID getAcademicTermId() {
        return academicTermId;
    }

    public UUID getCampusId() {
        return campusId;
    }

    public UUID getBellScheduleId() {
        return bellScheduleId;
    }

    public String getTimetableType() {
        return timetableType;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getGenerationMode() {
        return generationMode;
    }

    public String getModelCode() {
        return modelCode;
    }

    public String getObjectives() {
        return objectives;
    }

    public String getGenerationStatus() {
        return generationStatus;
    }

    public UUID getEaifRequestId() {
        return eaifRequestId;
    }

    public UUID getResultTimetableId() {
        return resultTimetableId;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
