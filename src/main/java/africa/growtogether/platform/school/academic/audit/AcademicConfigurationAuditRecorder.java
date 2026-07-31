package africa.growtogether.platform.school.academic.audit;


import africa.growtogether.platform.eiam.audit.*;

import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class AcademicConfigurationAuditRecorder {


    private final AuditEventService audit;


    public AcademicConfigurationAuditRecorder(
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
                        "ACADEMIC_CONFIGURATION",
                        resourceId,
                        message,
                        details == null ? Map.of() : details
                )
        );
    }
}
