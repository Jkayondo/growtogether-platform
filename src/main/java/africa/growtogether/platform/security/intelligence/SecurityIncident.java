package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "security_incidents",
        indexes = {
                @Index(
                        name = "ix_security_incident_tenant",
                        columnList = "tenant_id"
                ),
                @Index(
                        name = "ix_security_incident_status",
                        columnList = "tenant_id,status"
                )
        }
)
public class SecurityIncident extends AuditedTenantEntity {


    @Column(
            name = "security_alert_id",
            nullable = false
    )
    private UUID securityAlertId;


    @Column(
            name = "incident_number",
            nullable = false,
            length = 50,
            unique = true
    )
    private String incidentNumber;


    @Column(
            name = "title",
            nullable = false,
            length = 200
    )
    private String title;


    @Column(
            name = "description",
            columnDefinition = "text"
    )
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "severity",
            nullable = false,
            length = 20
    )
    private SecurityRiskAssessment.RiskLevel severity;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "incident_status",
            nullable = false,
            length = 30
    )
    private SecurityIncidentStatus status;


    @Column(
            name = "assigned_to"
    )
    private UUID assignedTo;


    @Column(
            name = "opened_at",
            nullable = false
    )
    private Instant openedAt;


    @Column(
            name = "resolved_at"
    )
    private Instant resolvedAt;


    protected SecurityIncident() {
    }


    public SecurityIncident(
            UUID tenantId,
            UUID securityAlertId,
            String incidentNumber,
            String title,
            String description,
            SecurityRiskAssessment.RiskLevel severity
    ) {

        setTenantId(tenantId);

        this.securityAlertId = securityAlertId;
        this.incidentNumber = incidentNumber;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = SecurityIncidentStatus.OPEN;
        this.openedAt = Instant.now();
    }


    public UUID getSecurityAlertId() {
        return securityAlertId;
    }


    public String getIncidentNumber() {
        return incidentNumber;
    }


    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }


    public SecurityRiskAssessment.RiskLevel getSeverity() {
        return severity;
    }


    public SecurityIncidentStatus getIncidentStatus() {
        return status;
    }


    public UUID getAssignedTo() {
        return assignedTo;
    }


    public Instant getOpenedAt() {
        return openedAt;
    }


    public Instant getResolvedAt() {
        return resolvedAt;
    }

   public void updateStatus(
           SecurityIncidentStatus status
  ) {
       this.status = status;
}

   public void updateIncidentStatus(
           SecurityIncidentStatus status
   ) {
       this.status = status;
   
}

}
