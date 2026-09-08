package africa.growtogether.platform.file;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.Map;
import java.util.UUID;



@Service
public class FileRetentionService {


    private final FileRecordRepository files;

    private final FileAuditRecorder audit;

    private final EnterpriseIdentityContext identity;



    public FileRetentionService(
            FileRecordRepository files,
            FileAuditRecorder audit,
            EnterpriseIdentityContext identity
    ) {

        this.files = files;
        this.audit = audit;
        this.identity = identity;

    }



    @Transactional
    public FileRecord setRetention(
            UUID id,
            Instant until
    ) {


        FileRecord file =
                get(id);


        file.applyRetention(
                until
        );


        audit.success(
                "FILE_RETENTION_UPDATED",
                file.storageKey(),
                "File retention updated",
                Map.of(
                        "retentionUntil",
                        until.toString()
                )
        );


        return file;

    }



    @Transactional
    public FileRecord legalHold(
            UUID id,
            boolean enabled
    ) {


        FileRecord file =
                get(id);


        if (enabled) {

            file.placeLegalHold();

        }
        else {

            file.releaseLegalHold();

        }


        audit.success(
                enabled
                        ? "FILE_LEGAL_HOLD_PLACED"
                        : "FILE_LEGAL_HOLD_RELEASED",
                file.storageKey(),
                "File legal hold updated",
                Map.of()
        );


        return file;

    }



    @Transactional
    public void dispose(
            UUID id
    ) {


        FileRecord file =
                get(id);


        file.validateDisposal(
                Instant.now()
        );


        audit.success(
                "FILE_DISPOSED",
                file.storageKey(),
                "File disposed",
                Map.of()
        );

    }



    private FileRecord get(
            UUID id
    ) {


        return files.findByIdAndTenantId(
                id,
                identity.requireTenantId()
        )
        .orElseThrow(
                () -> new IllegalArgumentException(
                        "File not found"
                )
        );

    }

}
