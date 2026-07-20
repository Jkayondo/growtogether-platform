package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.api.*;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integration/platform")
public class EipIntegrationController {
    private final EipAnalyticsService analytics;
    public EipIntegrationController(EipAnalyticsService analytics) { this.analytics = analytics; }

    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('integration.analytics.read')")
    public ApiResponse<Map<String,Object>> analytics() { return ApiResponses.success(analytics.snapshot()); }
}
