package africa.growtogether.platform.school.timetable.resource;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gts_scheduling_resource")
public class SchedulingResource
        extends AuditedTenantEntity {

    private static final Set<String> ALLOWED_RESOURCE_TYPES =
            Set.of(
                    "CLASSROOM",
                    "LABORATORY",
                    "WORKSHOP",
                    "LIBRARY",
                    "ICT_LAB",
                    "SPORTS_FIELD",
                    "HALL",
                    "ASSEMBLY_AREA",
                    "MUSIC_ROOM",
                    "ART_ROOM",
                    "SPECIAL_NEEDS_ROOM",
                    "ONLINE_ROOM",
                    "EQUIPMENT",
                    "OTHER"
            );

    @Column(name = "campus_id", nullable = false)
    private UUID campusId;

    @Column(name = "resource_code", nullable = false, length = 80)
    private String resourceCode;

    @Column(name = "resource_name", nullable = false, length = 200)
    private String resourceName;

    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;

    @Column
    private Integer capacity;

    @Column(name = "location_description", length = 300)
    private String locationDescription;

    @Column(name = "specialized_for_subject_id")
    private UUID specializedForSubjectId;

    @Column(nullable = false)
    private boolean bookable = true;

    @Column(name = "shared_resource", nullable = false)
    private boolean sharedResource = false;

    @Column(name = "resource_status", nullable = false, length = 30)
    private String resourceStatus = "ACTIVE";

    protected SchedulingResource() {
    }

    public SchedulingResource(
            UUID campusId,
            String resourceCode,
            String resourceName,
            String resourceType,
            Integer capacity,
            String locationDescription,
            UUID specializedForSubjectId,
            boolean bookable,
            boolean sharedResource
    ) {

        if (campusId == null) {
            throw new IllegalArgumentException(
                    "campusId must not be null"
            );
        }

        this.campusId = campusId;

        this.resourceCode =
                requireText(
                        resourceCode,
                        "resourceCode"
                );

        this.resourceName =
                requireText(
                        resourceName,
                        "resourceName"
                );

        String normalizedType =
                requireText(
                        resourceType,
                        "resourceType"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                !ALLOWED_RESOURCE_TYPES.contains(
                        normalizedType
                )
        ) {
            throw new IllegalArgumentException(
                    "Invalid resource type: "
                            + normalizedType
            );
        }

        if (
                capacity != null
                && capacity <= 0
        ) {
            throw new IllegalArgumentException(
                    "capacity must be greater than zero"
            );
        }

        this.resourceType = normalizedType;
        this.capacity = capacity;
        this.locationDescription = locationDescription;
        this.specializedForSubjectId = specializedForSubjectId;
        this.bookable = bookable;
        this.sharedResource = sharedResource;
    }

    public void activate() {
        this.resourceStatus = "ACTIVE";
    }

    public void markMaintenance() {
        this.resourceStatus = "MAINTENANCE";
    }

    public void markUnavailable() {
        this.resourceStatus = "UNAVAILABLE";
    }

    public void retire() {
        this.resourceStatus = "RETIRED";
    }

    public void archiveResource() {
        this.resourceStatus = "ARCHIVED";
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

    public String getResourceCode() {
        return resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public UUID getSpecializedForSubjectId() {
        return specializedForSubjectId;
    }

    public boolean isBookable() {
        return bookable;
    }

    public boolean isSharedResource() {
        return sharedResource;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }
}
