package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.events.AuditEventCreatedEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityFindingService {

    private final SecurityFindingRepository repository;
    private final SecurityAlertService alertService;


    public SecurityFindingService(
            SecurityFindingRepository repository,
            SecurityAlertService alertService
    ) {
        this.repository = repository;
        this.alertService = alertService;
    }


public SecurityFinding recordFinding(
            AuditEventCreatedEvent event,
            SecurityRiskAssessment assessment
    ) {

        SecurityFinding finding =
                new SecurityFinding(
                        event.tenantId(),
                        event.auditEventId(),
                        assessment
                );

        SecurityFinding saved =
                repository.save(finding);

        if (assessment.riskLevel()
                != SecurityRiskAssessment.RiskLevel.LOW) {

            alertService.createAlert(saved);
        }

        return saved;
    }

}
