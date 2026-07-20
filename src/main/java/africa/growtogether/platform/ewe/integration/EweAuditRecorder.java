package africa.growtogether.platform.ewe.integration;

import africa.growtogether.platform.eiam.audit.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** EWE adapter for the immutable enterprise audit service. */
@Component
public class EweAuditRecorder {
    private final AuditEventService audit;
    public EweAuditRecorder(AuditEventService audit) { this.audit = audit; }

    public void workflow(String eventType, UUID id, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.AUTHORIZATION,
            AuditOutcome.SUCCESS, SecuritySeverity.INFO, "WORKFLOW_INSTANCE", id.toString(), message, details));
    }

    public void failure(String eventType, UUID id, String message) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.SECURITY,
            AuditOutcome.FAILURE, SecuritySeverity.MEDIUM, "WORKFLOW_INSTANCE", id.toString(), message, Map.of()));
    }

    public void task(String eventType, UUID id, String message) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.AUTHORIZATION,
            AuditOutcome.SUCCESS, SecuritySeverity.INFO, "WORKFLOW_TASK", id.toString(), message, Map.of()));
    }
}
