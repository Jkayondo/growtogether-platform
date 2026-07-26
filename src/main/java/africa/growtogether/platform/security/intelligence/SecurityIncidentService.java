package africa.growtogether.platform.security.intelligence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Service
public class SecurityIncidentService {

    private final SecurityIncidentRepository repository;


    public SecurityIncidentService(
            SecurityIncidentRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SecurityIncident create(
            UUID tenantId,
            UUID securityAlertId,
            String incidentNumber,
            String title,
            String description,
            SecurityRiskAssessment.RiskLevel severity
    ) {

        SecurityIncident incident =
                new SecurityIncident(
                        tenantId,
                        securityAlertId,
                        incidentNumber,
                        title,
                        description,
                        severity
                );

        return repository.save(incident);
    }


    @Transactional
    public SecurityIncident resolve(
            SecurityIncident incident
    ) {

        incident.updateIncidentStatus(
                SecurityIncidentStatus.RESOLVED
        );

        return incident;
    }
}
