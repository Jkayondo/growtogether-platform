package africa.growtogether.platform.school.parent.api;


import africa.growtogether.platform.school.parent.ParentAcademicDashboard;
import africa.growtogether.platform.school.parent.ParentAcademicDashboardService;
import africa.growtogether.platform.school.parent.ParentAcademicView;
import africa.growtogether.platform.school.parent.ParentAcademicViewService;
import africa.growtogether.platform.school.parent.ParentAcademicAuthorizationService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/parent/academic")
public class ParentAcademicPortalController {


    private final ParentAcademicDashboardService dashboardService;

    private final ParentAcademicViewService viewService;

    private final ParentAcademicAuthorizationService authorizationService;


    public ParentAcademicPortalController(
            ParentAcademicDashboardService dashboardService,
            ParentAcademicViewService viewService,
            ParentAcademicAuthorizationService authorizationService
    ) {

        this.dashboardService = dashboardService;
        this.viewService = viewService;
        this.authorizationService = authorizationService;
    }


    @GetMapping("/{parentId}/dashboard")
    public ParentAcademicDashboard dashboard(
            @PathVariable UUID parentId
    ) {

        return dashboardService.loadDashboard(parentId);
    }


    @GetMapping("/{parentId}/view")
    public ParentAcademicView academicView(
            @PathVariable UUID parentId
    ) {

        return viewService.getParentAcademicView(parentId);
    }
}
