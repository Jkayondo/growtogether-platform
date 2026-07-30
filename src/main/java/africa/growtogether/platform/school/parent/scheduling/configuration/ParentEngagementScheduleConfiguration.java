package africa.growtogether.platform.school.parent.scheduling.configuration;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import africa.growtogether.platform.school.parent.reporting.ParentEngagementReportType;
import africa.growtogether.platform.school.parent.scheduling.ParentEngagementScheduleFrequency;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "parent_engagement_schedule_configurations")
public class ParentEngagementScheduleConfiguration
        extends AuditedTenantEntity {


    @Enumerated(EnumType.STRING)
    @Column(name = "report_type",
            nullable = false,
            length = 50)
    private ParentEngagementReportType reportType;


    @Enumerated(EnumType.STRING)
    @Column(name = "frequency",
            nullable = false,
            length = 30)
    private ParentEngagementScheduleFrequency frequency;


    @Column(name = "enabled",
            nullable = false)
    private boolean enabled;


    protected ParentEngagementScheduleConfiguration() {
    }


    public ParentEngagementScheduleConfiguration(
            UUID tenantId,
            ParentEngagementReportType reportType,
            ParentEngagementScheduleFrequency frequency
    ) {

        setTenantId(tenantId);

        this.reportType = reportType;
        this.frequency = frequency;
        this.enabled = true;
    }


    public void disable() {

        this.enabled = false;
    }


    public void enable() {

        this.enabled = true;
    }


    public boolean isEnabled() {

        return enabled;
    }


    public ParentEngagementReportType getReportType() {

        return reportType;
    }


    public ParentEngagementScheduleFrequency getFrequency() {

        return frequency;
    }
}
