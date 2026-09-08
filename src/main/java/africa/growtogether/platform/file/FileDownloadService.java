package africa.growtogether.platform.file;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;
import java.util.UUID;



@Service
public class FileDownloadService {


    private final FileRecordRepository repository;

    private final FileAuditRecorder audit;

    private final EnterpriseIdentityContext identity;



    public FileDownloadService(
            FileRecordRepository repository,
            FileAuditRecorder audit,
            EnterpriseIdentityContext identity
    ) {

        this.repository = repository;
        this.audit = audit;
        this.identity = identity;

    }



    @Transactional(readOnly = true)
    public FileRecord authorize(
            UUID id
    ) {


        UUID tenantId =
                identity.requireTenantId();



        FileRecord file =
                repository.findByIdAndTenantId(
                        id,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "File not found"
                        )
                );



        if (
                file.status()
                !=
                FileSecurityStatus.CLEAN
        ) {

            audit.denied(
                    "FILE_DOWNLOAD_DENIED",
                    file.storageKey(),
                    "File is not available for download"
            );


            throw new SecurityException(
                    "File is not available"
            );

        }



        audit.success(
                "FILE_DOWNLOAD_AUTHORIZED",
                file.storageKey(),
                "File download authorized",
                Map.of(
                        "tenantId",
                        tenantId.toString()
                )
        );



        return file;

    }

}
