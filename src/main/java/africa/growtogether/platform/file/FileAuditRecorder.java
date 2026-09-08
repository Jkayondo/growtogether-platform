package africa.growtogether.platform.file;


import africa.growtogether.platform.eiam.audit.*;

import org.springframework.stereotype.Component;

import java.util.Map;



@Component
public class FileAuditRecorder {


    private final AuditEventService audit;



    public FileAuditRecorder(
            AuditEventService audit
    ) {
        this.audit = audit;
    }



    public void success(
            String type,
            String fileReference,
            String message,
            Map<String,Object> details
    ) {

        audit.record(
                new RecordAuditEventCommand(
                        type,
                        AuditEventCategory.DATA_ACCESS,
                        AuditOutcome.SUCCESS,
                        SecuritySeverity.INFO,
                        "FILE",
                        fileReference,
                        message,
                        details == null
                                ? Map.of()
                                : details
                )
        );

    }



    public void denied(
            String type,
            String fileReference,
            String message
    ) {

        audit.record(
                new RecordAuditEventCommand(
                        type,
                        AuditEventCategory.SECURITY,
                        AuditOutcome.DENIED,
                        SecuritySeverity.HIGH,
                        "FILE",
                        fileReference,
                        message,
                        Map.of()
                )
        );

    }

}
