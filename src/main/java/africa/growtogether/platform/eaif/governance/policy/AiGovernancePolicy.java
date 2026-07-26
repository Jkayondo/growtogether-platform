package africa.growtogether.platform.eaif.governance.policy;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.eaif.AiEnums;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ai_governance_policies")
public class AiGovernancePolicy extends AuditedTenantEntity {


    @Column(name = "policy_code", nullable = false, length = 100)
    private String policyCode;


    @Column(name = "policy_name", nullable = false, length = 200)
    private String policyName;


    @Enumerated(EnumType.STRING)
    @Column(name = "maximum_risk_level", nullable = false, length = 20)
    private AiEnums.RiskLevel maximumRiskLevel;


    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;


    @Column(name = "active", nullable = false)
    private boolean active;


    protected AiGovernancePolicy() {
    }


    public AiGovernancePolicy(
            UUID tenantId,
            String policyCode,
            String policyName,
            AiEnums.RiskLevel maximumRiskLevel,
            boolean approvalRequired
    ) {

        setTenantId(tenantId);

        this.policyCode = policyCode;
        this.policyName = policyName;
        this.maximumRiskLevel = maximumRiskLevel;
        this.approvalRequired = approvalRequired;
        this.active = true;
    }


    public boolean allows(
            AiEnums.RiskLevel riskLevel
    ) {

        return active &&
                riskLevel.ordinal()
                <= maximumRiskLevel.ordinal();
    }


    public String policyCode() {
        return policyCode;
    }


    public boolean approvalRequired() {
        return approvalRequired;
    }


    public AiEnums.RiskLevel maximumRiskLevel() {
        return maximumRiskLevel;
    }


    public boolean active() {
        return active;
    }
}
