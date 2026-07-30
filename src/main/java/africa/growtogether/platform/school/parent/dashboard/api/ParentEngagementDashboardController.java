package africa.growtogether.platform.school.parent.dashboard.api;


import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboardService;
import africa.growtogether.platform.school.parent.dashboard.api.dto.ParentEngagementDashboardMapper;
import africa.growtogether.platform.school.parent.dashboard.api.dto.ParentEngagementDashboardResponse;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/parent-engagement")
public class ParentEngagementDashboardController {


    private final ParentEngagementDashboardService dashboardService;

    private final ParentEngagementDashboardMapper mapper;


    public ParentEngagementDashboardController(
            ParentEngagementDashboardService dashboardService,
            ParentEngagementDashboardMapper mapper
    ) {

        this.dashboardService = dashboardService;
        this.mapper = mapper;
    }


    @GetMapping("/{tenantId}/dashboard")
    public ParentEngagementDashboardResponse dashboard(
            @PathVariable UUID tenantId
    ) {

        return mapper.map(
                dashboardService.loadDashboard(tenantId)
        );
    }
}