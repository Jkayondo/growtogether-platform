package africa.growtogether.platform.school.leadership;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/leadership")
public class LeadershipOverviewController {

    private final LeadershipOverviewService service;

    public LeadershipOverviewController(
            LeadershipOverviewService service
    ) {
        this.service = service;
    }

    @GetMapping("/overview")
    @PreAuthorize(
            "hasAuthority('school.leadership.overview.read')"
    )
    public LeadershipOverviewResponse overview(
            @RequestHeader("X-Tenant-ID") UUID tenantId
    ) {
        return service.overview(tenantId);
    }
}
