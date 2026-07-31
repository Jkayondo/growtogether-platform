package africa.growtogether.platform.school.parent.audit;


import africa.growtogether.platform.eiam.audit.*;

import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class ParentEngagementAuditRecorder {


    private final AuditEventService audit;


    public ParentEngagementAuditRecorder(
            AuditEventService audit
    ) {

        this.audit = audit;
    }


    public void success(
            String eventType,
            String resourceId,
            String message,
            Map<String, Object> details
    ) {

        audit.record(
                new RecordAuditEventCommand(
                        eventType,
                        AuditEventCategory.DATA_ACCESS,
                        AuditOutcome.SUCCESS,
                        SecuritySeverity.INFO,
                        "PARENT_ENGAGEMENT",
                        resourceId,
                        message,
                        details == null ? Map.of() : details
                )
        );
    }
}
