package africa.growtogether.platform.school.parent.scheduling;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementScheduledReportService {


    private final ParentEngagementScheduledReportRepository repository;


    public ParentEngagementScheduledReportService(
            ParentEngagementScheduledReportRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementScheduledReport create(
            ParentEngagementScheduledReport report
    ) {

        return repository.save(report);
    }


    public long countActiveSchedules() {

        return repository
                .findByEnabledTrue()
                .size();
    }


    public void markGenerated(
            ParentEngagementScheduledReport report
    ) {

        report.markGenerated();

        repository.save(report);
    }
}
