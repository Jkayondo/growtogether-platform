package africa.growtogether.platform.eaif.governance.policy;

import africa.growtogether.platform.eaif.AiEnums;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AiGovernancePolicyService {

    private final AiGovernancePolicyRepository repository;


    public AiGovernancePolicyService(
            AiGovernancePolicyRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional(readOnly = true)
    public AiGovernancePolicy getPolicy(
            UUID tenantId,
            String policyCode
    ) {

        return repository
                .findByTenantIdAndPolicyCode(
                        tenantId,
                        policyCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "AI governance policy not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public boolean allows(
            UUID tenantId,
            String policyCode,
            AiEnums.RiskLevel riskLevel
    ) {

        AiGovernancePolicy policy =
                getPolicy(
                        tenantId,
                        policyCode
                );

        return policy.allows(riskLevel);
    }


    @Transactional(readOnly = true)
    public boolean requiresApproval(
            UUID tenantId,
            String policyCode
    ) {

        AiGovernancePolicy policy =
                getPolicy(
                        tenantId,
                        policyCode
                );

        return policy.approvalRequired();
    }
@Transactional(readOnly = true)
    public AiGovernanceDecision evaluate(
            UUID tenantId,
            String policyCode,
            AiEnums.RiskLevel riskLevel
    ) {

        AiGovernancePolicy policy =
                getPolicy(
                        tenantId,
                        policyCode
                );


        boolean allowed =
                policy.allows(riskLevel);


        boolean approvalRequired =
                policy.approvalRequired();


        String reason;

        if (allowed) {
            reason = "Risk level permitted by governance policy";
        } else {
            reason = "Risk level rejected by governance policy";
        }


        return new AiGovernanceDecision(
                policyCode,
                riskLevel,
                allowed,
                approvalRequired,
                reason
        );
    }
}

