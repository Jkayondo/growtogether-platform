package africa.growtogether.platform.file;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.UUID;



@Service
public class QuarantineService {


    private final QuarantinedFileRepository repository;

    private final FileAuditRecorder audit;

    private final EnterpriseIdentityContext identity;



    public QuarantineService(
            QuarantinedFileRepository repository,
            FileAuditRecorder audit,
            EnterpriseIdentityContext identity
    ) {

        this.repository = repository;
        this.audit = audit;
        this.identity = identity;

    }



    @Transactional
    public QuarantinedFile quarantine(
            UUID tenantId,
            String storageKey,
            String checksum,
            String mimeType,
            long sizeBytes,
            String reason
    ) {


        QuarantinedFile file =
                new QuarantinedFile(
                        tenantId,
                        storageKey,
                        checksum,
                        mimeType,
                        sizeBytes,
                        reason
                );


        QuarantinedFile saved =
                repository.save(
                        file
                );


        audit.success(
                "FILE_QUARANTINED",
                storageKey,
                "File moved to quarantine",
                Map.of(
                        "reason",
                        reason
                )
        );


        return saved;

    }



    @Transactional(readOnly = true)
    public List<QuarantinedFile> pending(
            UUID tenantId
    ) {

        return repository.findByTenantIdAndStatus(
                tenantId,
                FileSecurityStatus.QUARANTINED
        );

    }



    @Transactional
    public QuarantinedFile release(
            UUID id
    ) {


        UUID tenantId =
                identity.requireTenantId();


        QuarantinedFile file =
                repository.findById(
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Quarantined file not found"
                        )
                );


        file.release(
                identity.requireUserId()
        );


        QuarantinedFile saved =
                repository.save(
                        file
                );


        audit.success(
                "FILE_QUARANTINE_RELEASED",
                file.storageKey(),
                "File released from quarantine",
                Map.of(
                        "tenantId",
                        tenantId.toString()
                )
        );


        return saved;

    }



    @Transactional
    public QuarantinedFile reject(
            UUID id,
            String reason
    ) {


        UUID tenantId =
                identity.requireTenantId();


        QuarantinedFile file =
                repository.findById(
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Quarantined file not found"
                        )
                );


        file.reject(
                identity.requireUserId(),
                reason
        );


        QuarantinedFile saved =
                repository.save(
                        file
                );


        audit.success(
                "FILE_QUARANTINE_REJECTED",
                file.storageKey(),
                "File rejected from quarantine",
                Map.of(
                        "tenantId",
                        tenantId.toString(),
                        "reason",
                        reason == null ? "" : reason
                )
        );


        return saved;

    }

}
