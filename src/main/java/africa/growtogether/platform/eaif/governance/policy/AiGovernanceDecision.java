package africa.growtogether.platform.eaif.governance.policy;

import africa.growtogether.platform.eaif.AiEnums;

public record AiGovernanceDecision(
        String policyCode,
        AiEnums.RiskLevel riskLevel,
        boolean allowed,
        boolean approvalRequired,
        String reason
) {
}
