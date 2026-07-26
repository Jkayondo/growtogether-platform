package africa.growtogether.platform.security.intelligence;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class SecurityIncidentAutomationService {


    private final SecurityIncidentService incidentService;


    public SecurityIncidentAutomationService(
            SecurityIncidentService incidentService
    ) {
        this.incidentService = incidentService;
    }


    public SecurityIncident processAlert(
            SecurityAlert alert
    ) {

        if (!requiresIncident(alert)) {
            return null;
        }


        return incidentService.create(
                alert.getTenantId(),
                alert.getId(),
                generateIncidentNumber(),
                "Automated Security Investigation",
                "Incident automatically created from security alert.",
                alert.getSeverity()
        );
    }


    private boolean requiresIncident(
            SecurityAlert alert
    ) {

        return alert.getSeverity()
                == SecurityRiskAssessment.RiskLevel.CRITICAL
                ||
                alert.getSeverity()
                == SecurityRiskAssessment.RiskLevel.HIGH;
    }


    private String generateIncidentNumber() {

        return "INC-"
                + UUID.randomUUID()
                .toString()
                .substring(0,8)
                .toUpperCase();
    }
}
