package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.eiam.audit.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EdsAuditRecorder {
    private final AuditEventService audit;
    public EdsAuditRecorder(AuditEventService audit) { this.audit = audit; }
    public void success(String type, UUID documentId, String message, Map<String,Object> details) {
        audit.record(new RecordAuditEventCommand(type, AuditEventCategory.DATA_ACCESS, AuditOutcome.SUCCESS,
            SecuritySeverity.INFO, "DOCUMENT", documentId.toString(), message, details == null ? Map.of() : details));
    }
    public void denied(String type, UUID documentId, String message) {
        audit.record(new RecordAuditEventCommand(type, AuditEventCategory.SECURITY, AuditOutcome.DENIED,
            SecuritySeverity.HIGH, "DOCUMENT", documentId.toString(), message, Map.of()));
    }
}
