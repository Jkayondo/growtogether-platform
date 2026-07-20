package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.eiam.audit.*;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Single enterprise-audit adapter for EIP. Payloads and credentials are never written to audit details. */
@Component
public class EipAuditRecorder {
    private final AuditEventService audit;
    public EipAuditRecorder(AuditEventService audit) { this.audit = audit; }

    public void success(String type, String resourceType, String resourceId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(type, AuditEventCategory.INTEGRATION, AuditOutcome.SUCCESS,
            SecuritySeverity.INFO, resourceType, resourceId, message, details == null ? Map.of() : details));
    }

    public void failure(String type, String resourceType, String resourceId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(type, AuditEventCategory.INTEGRATION, AuditOutcome.FAILURE,
            SecuritySeverity.MEDIUM, resourceType, resourceId, message, details == null ? Map.of() : details));
    }
}
