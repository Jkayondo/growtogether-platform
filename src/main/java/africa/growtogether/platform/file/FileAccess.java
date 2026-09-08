package africa.growtogether.platform.file;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;



@Entity
@Table(
        name = "file_accesses",
        indexes = {
                @Index(
                        name = "ix_file_access_token",
                        columnList = "token_hash",
                        unique = true
                )
        }
)
public class FileAccess extends AuditedTenantEntity {


    @Column(
            name = "file_id",
            nullable = false
    )
    private UUID fileId;



    @Column(
            name = "token_hash",
            nullable = false,
            length = 64,
            unique = true
    )
    private String tokenHash;



    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;



    @Column(
            name = "download_count",
            nullable = false
    )
    private int downloadCount = 0;



    @Column(
            name = "max_downloads"
    )
    private Integer maxDownloads;



    @Column(
            name = "revoked_at"
    )
    private Instant revokedAt;



    protected FileAccess() {
    }



    public FileAccess(
            UUID tenantId,
            UUID fileId,
            String tokenHash,
            Instant expiresAt,
            Integer maxDownloads
    ) {

        setTenantId(
                tenantId
        );

        this.fileId = fileId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.maxDownloads = maxDownloads;

    }



    public boolean validAt(
            Instant now
    ) {

        return revokedAt == null
                &&
                expiresAt.isAfter(now)
                &&
                (
                        maxDownloads == null
                        ||
                        downloadCount < maxDownloads
                );

    }



    public void recordDownload() {

        if (
                maxDownloads != null
                &&
                downloadCount >= maxDownloads
        ) {

            throw new IllegalStateException(
                    "Download limit reached"
            );

        }


        downloadCount++;

    }



    public UUID fileId() {
        return fileId;
    }


    public String tokenHash() {
        return tokenHash;
    }


    public Instant expiresAt() {
        return expiresAt;
    }

}
