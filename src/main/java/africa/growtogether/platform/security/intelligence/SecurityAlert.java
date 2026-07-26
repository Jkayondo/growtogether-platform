package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert extends AuditedTenantEntity {

    @Column(name = "security_finding_id", nullable = false)
    private UUID securityFindingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private SecurityRiskAssessment.RiskLevel severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false, length = 20)
    private AlertStatus alertStatus;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "alert_created_at", nullable = false)
    private Instant alertCreatedAt;


    protected SecurityAlert() {
    }


    public SecurityAlert(
            UUID tenantId,
            SecurityFinding finding
    ) {

        setTenantId(tenantId);

        this.securityFindingId = finding.getId();
        this.severity = finding.getRiskLevel();
        this.alertStatus = AlertStatus.OPEN;
        this.message = finding.getReason();
        this.alertCreatedAt = Instant.now();
    }


    public UUID getSecurityFindingId() {
        return securityFindingId;
    }

    public SecurityRiskAssessment.RiskLevel getSeverity() {
        return severity;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public String getMessage() {
        return message;
    }
}
