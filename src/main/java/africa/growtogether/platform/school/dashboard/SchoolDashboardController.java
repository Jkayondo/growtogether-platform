package africa.growtogether.platform.school.dashboard;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/dashboard")
public class SchoolDashboardController {


    private final SchoolDashboardService service;


    public SchoolDashboardController(
            SchoolDashboardService service
    ) {

        this.service = service;

    }


    @GetMapping
    public SchoolDashboardResponse dashboard(
            @RequestHeader("X-Tenant-ID") UUID tenantId
    ) {

        return service.getDashboard(
                tenantId
        );

    }

}