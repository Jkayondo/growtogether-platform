package africa.growtogether.platform.file;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;



@Service
public class FileUploadService {


    private final FileStorageProvider storage;

    private final FileValidationService validation;

    private final FileSecurityScanner scanner;

    private final FileAuditRecorder audit;

    private final QuarantineService quarantine;

    private final EnterpriseIdentityContext identity;



    public FileUploadService(
            FileStorageProvider storage,
            FileValidationService validation,
            FileSecurityScanner scanner,
            FileAuditRecorder audit,
            QuarantineService quarantine,
            EnterpriseIdentityContext identity
    ) {

        this.storage = storage;
        this.validation = validation;
        this.scanner = scanner;
        this.audit = audit;
        this.quarantine = quarantine;
        this.identity = identity;

    }



    public FileUploadResult upload(
            MultipartFile file
    ) {


        String fileReference =
                file != null
                        ? file.getOriginalFilename()
                        : "UNKNOWN";



        audit.success(
                "FILE_UPLOAD_REQUESTED",
                fileReference,
                "File upload requested",
                Map.of()
        );



        validation.validate(
                file
        );



        audit.success(
                "FILE_VALIDATION_PASSED",
                fileReference,
                "File validation passed",
                Map.of(
                        "sizeBytes",
                        file.getSize()
                )
        );



        FileSecurityScanResult scanResult =
                scanner.scan(
                        file
                );



        if (
                scanResult.status()
                !=
                FileSecurityScanResult.Status.CLEAN
        ) {


            String checksum =
                    checksum(file);



            UUID tenantId =
                    identity.requireTenantId();



            String storageKey =
                    "quarantine/"
                    + tenantId
                    + "/"
                    + fileReference;



            quarantine.quarantine(
                    tenantId,
                    storageKey,
                    checksum,
                    file.getContentType(),
                    file.getSize(),
                    scanResult.message()
            );


            audit.denied(
                    "FILE_SECURITY_SCAN_FAILED",
                    fileReference,
                    scanResult.message()
            );


            throw new IllegalStateException(
                    "File quarantined: "
                    + scanResult.message()
            );

        }



        audit.success(
                "FILE_SECURITY_SCAN_PASSED",
                fileReference,
                "File security scan passed",
                Map.of()
        );



        String storageKey =
                storage.store(
                        identity.requireTenantId(),
                        file
                );



        audit.success(
                "FILE_STORED",
                storageKey,
                "File stored successfully",
                Map.of(
                        "sizeBytes",
                        file.getSize()
                )
        );



        return new FileUploadResult(
                storageKey,
                checksum(file),
                file.getContentType(),
                file.getSize()
        );

    }



    private String checksum(
            MultipartFile file
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] hash =
                    digest.digest(
                            file.getBytes()
                    );


            StringBuilder builder =
                    new StringBuilder();



            for (
                    byte value : hash
            ) {

                builder.append(
                        String.format(
                                "%02x",
                                value
                        )
                );

            }



            return builder.toString();


        }
        catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to calculate checksum",
                    e
            );

        }

    }

}
