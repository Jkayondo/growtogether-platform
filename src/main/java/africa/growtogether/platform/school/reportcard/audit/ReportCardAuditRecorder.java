package africa.growtogether.platform.school.reportcard.audit;


import africa.growtogether.platform.eiam.audit.AuditEventCategory;
import africa.growtogether.platform.eiam.audit.AuditEventService;
import africa.growtogether.platform.eiam.audit.AuditOutcome;
import africa.growtogether.platform.eiam.audit.RecordAuditEventCommand;
import africa.growtogether.platform.eiam.audit.SecuritySeverity;

import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class ReportCardAuditRecorder {


    private final AuditEventService audit;


    public ReportCardAuditRecorder(
            AuditEventService audit
    ) {

        this.audit = audit;
    }


    public void success(
            String eventType,
            String resourceId,
            String message,
            Map<String,Object> details
    ) {

        audit.record(
                new RecordAuditEventCommand(
                        eventType,
                        AuditEventCategory.DATA_ACCESS,
                        AuditOutcome.SUCCESS,
                        SecuritySeverity.INFO,
                        "REPORT_CARD",
                        resourceId,
                        message,
                        details == null ? Map.of() : details
                )
        );
    }
}
