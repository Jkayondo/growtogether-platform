package africa.growtogether.platform.file;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;



@Service
public class FileAccessService {


    private final FileAccessRepository repository;

    private final FileRecordRepository files;

    private final FileAuditRecorder audit;

    private final EnterpriseIdentityContext identity;



    private final SecureRandom random =
            new SecureRandom();



    public FileAccessService(
            FileAccessRepository repository,
            FileRecordRepository files,
            FileAuditRecorder audit,
            EnterpriseIdentityContext identity
    ) {

        this.repository = repository;
        this.files = files;
        this.audit = audit;
        this.identity = identity;

    }



    @Transactional
    public String create(
            UUID fileId
    ) {


        UUID tenantId =
                identity.requireTenantId();



        String token =
                generateToken();



        FileAccess access =
                new FileAccess(
                        tenantId,
                        fileId,
                        hash(token),
                        Instant.now()
                                .plus(
                                        24,
                                        ChronoUnit.HOURS
                                ),
                        10
                );



        repository.save(
                access
        );



        audit.success(
                "FILE_ACCESS_CREATED",
                fileId.toString(),
                "Temporary file access created",
                Map.of()
        );



        return token;

    }



    @Transactional
    public FileAccess validate(
            String token
    ) {


        FileAccess access =
                repository.findByTokenHash(
                        hash(token)
                )
                .orElseThrow(
                        () -> new SecurityException(
                                "Invalid file access token"
                        )
                );



        if (
                !access.validAt(
                        Instant.now()
                )
        ) {

            throw new SecurityException(
                    "File access expired or revoked"
            );

        }



        access.recordDownload();


        return repository.save(
                access
        );

    }



    @Transactional
    public FileRecord resolveFile(
            String token
    ) {


        FileAccess access =
                validate(
                        token
                );


        FileRecord file =
                files.findById(
                        access.fileId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "File not found"
                        )
                );


        audit.success(
                "FILE_SIGNED_DOWNLOAD_AUTHORIZED",
                file.storageKey(),
                "Signed file download authorized",
                Map.of()
        );


        return file;

    }




    private String generateToken() {

        byte[] bytes =
                new byte[32];


        random.nextBytes(
                bytes
        );


        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );

    }



    private String hash(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] result =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            StringBuilder builder =
                    new StringBuilder();


            for (
                    byte b : result
            ) {

                builder.append(
                        String.format(
                                "%02x",
                                b
                        )
                );

            }


            return builder.toString();

        }
        catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to hash token",
                    e
            );

        }

    }

}
