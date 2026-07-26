package africa.growtogether.platform.security.intelligence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityAlertQueryService {

    private final SecurityAlertRepository repository;


    public SecurityAlertQueryService(
            SecurityAlertRepository repository
    ) {
        this.repository = repository;
    }


    public Page<SecurityAlert> findAlerts(
            UUID tenantId,
            int page,
            int size
    ) {

        int safeSize = Math.min(Math.max(size, 1), 100);

        return repository.findByTenantId(
                tenantId,
                PageRequest.of(page, safeSize)
        );
    }
}
