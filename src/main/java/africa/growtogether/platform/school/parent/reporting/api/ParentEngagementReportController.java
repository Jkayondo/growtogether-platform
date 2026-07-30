package africa.growtogether.platform.school.parent.reporting.api;


import africa.growtogether.platform.school.parent.reporting.ParentEngagementReportService;
import africa.growtogether.platform.school.parent.reporting.ParentEngagementReportType;
import africa.growtogether.platform.school.parent.reporting.api.dto.ParentEngagementReportMapper;
import africa.growtogether.platform.school.parent.reporting.api.dto.ParentEngagementReportResponse;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/parent-engagement/reports")
public class ParentEngagementReportController {


    private final ParentEngagementReportService reportService;

    private final ParentEngagementReportMapper mapper;


    public ParentEngagementReportController(
            ParentEngagementReportService reportService,
            ParentEngagementReportMapper mapper
    ) {

        this.reportService = reportService;
        this.mapper = mapper;
    }


    @GetMapping("/{tenantId}/{type}")
    public ParentEngagementReportResponse report(
            @PathVariable UUID tenantId,
            @PathVariable ParentEngagementReportType type
    ) {

        return mapper.map(
                reportService.generate(
                        tenantId,
                        type
                )
        );
    }
}
