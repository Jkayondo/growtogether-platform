package africa.growtogether.platform.eaif.governance.policy;

import africa.growtogether.platform.eaif.AiEnums;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiGovernancePolicyRepository
        extends JpaRepository<AiGovernancePolicy, UUID> {


    Optional<AiGovernancePolicy>
    findByTenantIdAndPolicyCode(
            UUID tenantId,
            String policyCode
    );


    List<AiGovernancePolicy>
    findByTenantIdAndActiveTrue(
            UUID tenantId
    );


    List<AiGovernancePolicy>
    findByTenantIdAndMaximumRiskLevel(
            UUID tenantId,
            AiEnums.RiskLevel maximumRiskLevel
    );
}
