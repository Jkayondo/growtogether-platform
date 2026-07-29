package africa.growtogether.platform.school.onboarding;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "school_onboarding_workflows",
        indexes = {
                @Index(
                        name = "ix_school_onboarding_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class SchoolOnboardingWorkflow
        extends AuditedTenantEntity {


    @Column(
            name = "school_configuration_id",
            nullable = false
    )
    private UUID schoolConfigurationId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "onboarding_status",
            nullable = false,
            length = 50
    )
    private SchoolOnboardingStatus status;


    @Column(
            name = "started_at",
            nullable = false
    )
    private Instant startedAt;


    @Column(
            name = "completed_at"
    )
    private Instant completedAt;


    protected SchoolOnboardingWorkflow() {
    }


    public SchoolOnboardingWorkflow(
            UUID tenantId,
            UUID schoolConfigurationId
    ) {

        setTenantId(tenantId);

        this.schoolConfigurationId = schoolConfigurationId;
        this.status = SchoolOnboardingStatus.NEW;
        this.startedAt = Instant.now();
    }


    public UUID getSchoolConfigurationId() {
        return schoolConfigurationId;
    }


    public SchoolOnboardingStatus getOnboardingStatus() {
        return status;
    }


    public Instant getStartedAt() {
        return startedAt;
    }


    public Instant getCompletedAt() {
        return completedAt;
    }


    public void updateOnboardingStatus(
            SchoolOnboardingStatus status
    ) {

        this.status = status;

        if (status == SchoolOnboardingStatus.ACTIVE) {
            this.completedAt = Instant.now();
        }
    }
}
