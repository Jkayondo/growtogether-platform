package africa.growtogether.platform.security.intelligence;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/security/alerts")
public class SecurityAlertController {

    private final SecurityAlertQueryService service;
    private final EnterpriseIdentityContext identityContext;


    public SecurityAlertController(
            SecurityAlertQueryService service,
            EnterpriseIdentityContext identityContext
    ) {
        this.service = service;
        this.identityContext = identityContext;
    }


    @GetMapping
    public Page<SecurityAlert> findAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        requireAlertViewPermission();

        return service.findAlerts(
                identityContext.requireTenantId(),
                page,
                size
        );
    }


    private void requireAlertViewPermission() {

        if (!identityContext.hasPermission(
                "SECURITY_ALERT_VIEW"
        )) {
            throw new AccessDeniedException(
                    "Missing SECURITY_ALERT_VIEW permission."
            );
        }
    }
}
