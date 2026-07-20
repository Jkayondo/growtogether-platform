package africa.growtogether.platform.eap.integration;

import africa.growtogether.platform.eiam.audit.*;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Enterprise audit adapter. Raw analytics payloads are intentionally excluded. */
@Component
public class EapAuditRecorder {
    private final AuditEventService audit;
    public EapAuditRecorder(AuditEventService audit) { this.audit = audit; }

    public void success(String eventType, String resourceType, String resourceId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.DATA_ACCESS, AuditOutcome.SUCCESS,
            SecuritySeverity.INFO, resourceType, resourceId, message, details == null ? Map.of() : details));
    }

    public void failure(String eventType, String resourceType, String resourceId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.DATA_ACCESS, AuditOutcome.FAILURE,
            SecuritySeverity.MEDIUM, resourceType, resourceId, message, details == null ? Map.of() : details));
    }
}
