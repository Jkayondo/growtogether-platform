package africa.growtogether.platform.security.intelligence;

import org.springframework.stereotype.Service;

@Service
public class SecurityAlertService {

    private final SecurityAlertRepository repository;


    public SecurityAlertService(
            SecurityAlertRepository repository
    ) {
        this.repository = repository;
    }


    public SecurityAlert createAlert(
            SecurityFinding finding
    ) {

        SecurityAlert alert =
                new SecurityAlert(
                        finding.getTenantId(),
                        finding
                );

        return repository.save(alert);
    }
}
