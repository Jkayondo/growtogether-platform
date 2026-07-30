package africa.growtogether.platform.school.parent.export;


import africa.growtogether.platform.school.parent.reporting.ParentEngagementReport;

import org.springframework.stereotype.Service;


@Service
public class ParentEngagementExportService {


    public ParentEngagementExportResult export(
            ParentEngagementReport report,
            ParentEngagementExportFormat format
    ) {


        String content =
                buildContent(report);


        String fileName =
                "parent-engagement-report."
                        +
                        format.name()
                                .toLowerCase();


        return new ParentEngagementExportResult(
                format,
                fileName,
                content
        );
    }


    private String buildContent(
            ParentEngagementReport report
    ) {

        return """
                Report Type,%s
                Total Events,%d
                Delivered,%d
                Viewed,%d
                Acknowledged,%d
                """
                .formatted(
                        report.getReportType(),
                        report.getTotalEvents(),
                        report.getDelivered(),
                        report.getViewed(),
                        report.getAcknowledged()
                );
    }
}
