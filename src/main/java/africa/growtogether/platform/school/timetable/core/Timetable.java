package africa.growtogether.platform.school.timetable.core;

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
@Table(name = "gts_timetable")
public class Timetable extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "MASTER",
                    "CLASS",
                    "TEACHER",
                    "ROOM",
                    "EXAMINATION",
                    "BOARDING",
                    "ACTIVITY",
                    "OTHER"
            );

    private static final Set<String> ALLOWED_GENERATORS =
            Set.of(
                    "MANUAL",
                    "RULE_ENGINE",
                    "AI_ASSISTED",
                    "IMPORTED"
            );

    @Column(name = "timetable_code", nullable = false, length = 100)
    private String timetableCode;

    @Column(name = "timetable_name", nullable = false, length = 250)
    private String timetableName;

    @Column(length = 1500)
    private String description;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "academic_term_id")
    private UUID academicTermId;

    @Column(name = "campus_id", nullable = false)
    private UUID campusId;

    @Column(name = "bell_schedule_id", nullable = false)
    private UUID bellScheduleId;

    @Column(name = "timetable_type", nullable = false, length = 30)
    private String timetableType;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "generated_by", nullable = false, length = 30)
    private String generatedBy;

    @Column(name = "generation_reference")
    private UUID generationReference;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "timetable_status", nullable = false, length = 30)
    private String timetableStatus = "DRAFT";

    protected Timetable() {
    }

    public Timetable(
            String timetableCode,
            String timetableName,
            String description,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID bellScheduleId,
            String timetableType,
            Integer versionNumber,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String generatedBy,
            UUID generationReference,
            UUID workflowInstanceId
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

        if (
                versionNumber == null
                || versionNumber <= 0
        ) {
            throw new IllegalArgumentException(
                    "versionNumber must be greater than zero"
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
                        timetableType,
                        "timetableType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (!ALLOWED_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException(
                    "Invalid timetable type: "
                            + normalizedType
            );
        }

        String normalizedGenerator =
                generatedBy == null
                        || generatedBy.isBlank()
                        ? "MANUAL"
                        : generatedBy.trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );

        if (
                !ALLOWED_GENERATORS.contains(
                        normalizedGenerator
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid timetable generator: "
                            + normalizedGenerator
            );
        }

        this.timetableCode =
                requireText(
                        timetableCode,
                        "timetableCode"
                );

        this.timetableName =
                requireText(
                        timetableName,
                        "timetableName"
                );

        this.description = description;
        this.academicYearId = academicYearId;
        this.academicTermId = academicTermId;
        this.campusId = campusId;
        this.bellScheduleId = bellScheduleId;
        this.timetableType = normalizedType;
        this.versionNumber = versionNumber;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.generatedBy = normalizedGenerator;
        this.generationReference = generationReference;
        this.workflowInstanceId = workflowInstanceId;

        /*
         * Non-manual timetables already represent generated output.
         * Manual timetables begin as drafts.
         */
        this.timetableStatus =
                "MANUAL".equals(normalizedGenerator)
                        ? "DRAFT"
                        : "GENERATED";
    }

    public void submitForReview() {

        if (
                !"DRAFT".equals(timetableStatus)
                && !"GENERATED".equals(timetableStatus)
        ) {
            throw new IllegalStateException(
                    "Only DRAFT or GENERATED timetables can be submitted for review"
            );
        }

        this.timetableStatus = "UNDER_REVIEW";
    }

    public void approve(
            UUID approvedBy
    ) {

        if (approvedBy == null) {
            throw new IllegalArgumentException(
                    "approvedBy must not be null"
            );
        }

        if (!"UNDER_REVIEW".equals(timetableStatus)) {
            throw new IllegalStateException(
                    "Only UNDER_REVIEW timetables can be approved"
            );
        }

        this.approvedBy = approvedBy;
        this.approvedAt = Instant.now();
        this.timetableStatus = "APPROVED";
    }

    public void publish(
            UUID publishedBy
    ) {

        if (publishedBy == null) {
            throw new IllegalArgumentException(
                    "publishedBy must not be null"
            );
        }

        if (!"APPROVED".equals(timetableStatus)) {
            throw new IllegalStateException(
                    "Only APPROVED timetables can be published"
            );
        }

        this.publishedBy = publishedBy;
        this.publishedAt = Instant.now();
        this.timetableStatus = "PUBLISHED";
    }

    public void activate() {

        if (!"PUBLISHED".equals(timetableStatus)) {
            throw new IllegalStateException(
                    "Only PUBLISHED timetables can be activated"
            );
        }

        this.timetableStatus = "ACTIVE";
    }

    public void suspend() {

        if (!"ACTIVE".equals(timetableStatus)) {
            throw new IllegalStateException(
                    "Only ACTIVE timetables can be suspended"
            );
        }

        this.timetableStatus = "SUSPENDED";
    }

    public void supersede() {

        if (
                !"PUBLISHED".equals(timetableStatus)
                && !"ACTIVE".equals(timetableStatus)
                && !"SUSPENDED".equals(timetableStatus)
        ) {
            throw new IllegalStateException(
                    "Timetable cannot be superseded from status "
                            + timetableStatus
            );
        }

        this.timetableStatus = "SUPERSEDED";
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

    public String getTimetableCode() {
        return timetableCode;
    }

    public String getTimetableName() {
        return timetableName;
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

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public UUID getGenerationReference() {
        return generationReference;
    }

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public UUID getPublishedBy() {
        return publishedBy;
    }

    public String getTimetableStatus() {
        return timetableStatus;
    }
}
