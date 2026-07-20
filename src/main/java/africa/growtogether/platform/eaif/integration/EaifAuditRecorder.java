package africa.growtogether.platform.eaif.integration;

import africa.growtogether.platform.eiam.audit.*;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Sanitized enterprise audit adapter. Prompts, credentials and model outputs are excluded. */
@Component
public class EaifAuditRecorder {
    private final AuditEventService audit;
    public EaifAuditRecorder(AuditEventService audit) { this.audit = audit; }

    public void success(String eventType, String resourceId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.SECURITY, AuditOutcome.SUCCESS,
            SecuritySeverity.INFO, "AI_REQUEST", resourceId, message, details == null ? Map.of() : details));
    }

    public void failure(String eventType, String resourceId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.SECURITY, AuditOutcome.FAILURE,
            SecuritySeverity.MEDIUM, "AI_REQUEST", resourceId, message, details == null ? Map.of() : details));
    }
}
