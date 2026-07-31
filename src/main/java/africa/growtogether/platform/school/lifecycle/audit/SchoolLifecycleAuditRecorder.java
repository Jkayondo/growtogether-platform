package africa.growtogether.platform.school.lifecycle.audit;


import africa.growtogether.platform.eiam.audit.*;

import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class SchoolLifecycleAuditRecorder {


    private final AuditEventService audit;


    public SchoolLifecycleAuditRecorder(
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
                        AuditEventCategory.TENANT_ADMINISTRATION,
                        AuditOutcome.SUCCESS,
                        SecuritySeverity.INFO,
                        "SCHOOL_LIFECYCLE",
                        resourceId,
                        message,
                        details == null ? Map.of() : details
                )
        );
    }


    public void failure(
            String eventType,
            String resourceId,
            String message,
            Map<String, Object> details
    ) {

        audit.record(
                new RecordAuditEventCommand(
                        eventType,
                        AuditEventCategory.TENANT_ADMINISTRATION,
                        AuditOutcome.FAILURE,
                        SecuritySeverity.MEDIUM,
                        "SCHOOL_LIFECYCLE",
                        resourceId,
                        message,
                        details == null ? Map.of() : details
                )
        );
    }
}
