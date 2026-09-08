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
                        auditResourceId(fileReference),
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
                        auditResourceId(fileReference),
                        message,
                        Map.of()
                )
        );

    }


    /**
     * Keep audit identifiers within the existing 100-character schema.
     * Long references use a stable digest instead of lossy truncation.
     * The original storage key remains in the storage/document records.
     */
    private static String auditResourceId(String reference) {
        if (reference == null || reference.length() <= 100) {
            return reference;
        }
        try {
            byte[] digest = java.security.MessageDigest
                    .getInstance("SHA-256")
                    .digest(reference.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "sha256:" + java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
