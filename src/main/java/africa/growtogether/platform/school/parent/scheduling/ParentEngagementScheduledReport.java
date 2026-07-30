package africa.growtogether.platform.school.parent.scheduling;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import africa.growtogether.platform.school.parent.reporting.ParentEngagementReportType;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "parent_engagement_scheduled_reports")
public class ParentEngagementScheduledReport
        extends AuditedTenantEntity {


    @Enumerated(EnumType.STRING)
    @Column(
            name = "frequency",
            nullable = false,
            length = 30
    )
    private ParentEngagementScheduleFrequency frequency;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "report_type",
            nullable = false,
            length = 50
    )
    private ParentEngagementReportType reportType;


    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled;


    @Column(
            name = "last_generated_at"
    )
    private Instant lastGeneratedAt;


    protected ParentEngagementScheduledReport() {
    }


    public ParentEngagementScheduledReport(
            UUID tenantId,
            ParentEngagementScheduleFrequency frequency,
            ParentEngagementReportType reportType
    ) {

        setTenantId(tenantId);

        this.frequency = frequency;
        this.reportType = reportType;
        this.enabled = true;
    }


    public void markGenerated() {

        this.lastGeneratedAt = Instant.now();
    }


    public boolean isEnabled() {
        return enabled;
    }


    public ParentEngagementScheduleFrequency getFrequency() {
        return frequency;
    }


    public ParentEngagementReportType getReportType() {
        return reportType;
    }
}
