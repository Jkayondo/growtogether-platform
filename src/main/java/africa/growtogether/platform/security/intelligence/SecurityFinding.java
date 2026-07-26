package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_findings")
public class SecurityFinding extends AuditedTenantEntity {

    @Column(name = "audit_event_id", nullable = false)
    private UUID auditEventId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private SecurityRiskAssessment.RiskLevel riskLevel;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;


    protected SecurityFinding() {
    }


    public SecurityFinding(
            UUID tenantId,
            UUID auditEventId,
            SecurityRiskAssessment assessment
    ) {
        setTenantId(tenantId);
        this.auditEventId = auditEventId;
        this.riskScore = assessment.riskScore();
        this.riskLevel = assessment.riskLevel();
        this.reason = assessment.reason();
        this.detectedAt = Instant.now();
    }


    public UUID getAuditEventId() {
        return auditEventId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public SecurityRiskAssessment.RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getReason() {
        return reason;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }
}
