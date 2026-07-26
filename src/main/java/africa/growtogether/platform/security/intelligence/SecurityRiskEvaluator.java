package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.events.AuditEventCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityRiskEvaluator {

    public SecurityRiskAssessment evaluate(AuditEventCreatedEvent event) {

        int score = switch (event.outcome()) {
            case SUCCESS -> 5;
            case FAILURE -> 40;
            case DENIED -> 60;
        };

        SecurityRiskAssessment.RiskLevel level =
                score >= 80 ? SecurityRiskAssessment.RiskLevel.CRITICAL :
                score >= 60 ? SecurityRiskAssessment.RiskLevel.HIGH :
                score >= 30 ? SecurityRiskAssessment.RiskLevel.MEDIUM :
                SecurityRiskAssessment.RiskLevel.LOW;

        return new SecurityRiskAssessment(
                score,
                level,
                "Calculated from audit event outcome: " + event.outcome()
        );
    }
}
