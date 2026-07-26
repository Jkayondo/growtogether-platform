package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.events.AuditEventCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private final SecurityRiskEvaluator evaluator;
    private final SecurityFindingService findingService;


    public SecurityEventListener(
            SecurityRiskEvaluator evaluator,
            SecurityFindingService findingService
    ) {
        this.evaluator = evaluator;
        this.findingService = findingService;
    }


    @EventListener
    public void handle(AuditEventCreatedEvent event) {

        SecurityRiskAssessment assessment =
                evaluator.evaluate(event);

        findingService.recordFinding(
                event,
                assessment
        );
    }
}
