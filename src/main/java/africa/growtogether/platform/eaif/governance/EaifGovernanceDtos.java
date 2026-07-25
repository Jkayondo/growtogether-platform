package africa.growtogether.platform.eaif.governance;

import africa.growtogether.platform.eaif.audit.ExecutionStatus;
import africa.growtogether.platform.eaif.AiEnums;

import java.util.Map;

public final class EaifGovernanceDtos {

    private EaifGovernanceDtos(){}


    public record Summary(
            long totalExecutions,
            long completedExecutions,
            long failedExecutions,
            long rejectedExecutions,
            Map<ExecutionStatus, Long> statusDistribution,
            Map<AiEnums.RiskLevel, Long> riskDistribution
    ) {}
}
