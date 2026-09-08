package africa.growtogether.platform.file;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;



@Entity
@Table(
        name = "file_quarantined_files"
)
public class QuarantinedFile extends AuditedTenantEntity {


    @Column(
            name = "storage_key",
            nullable = false,
            length = 500
    )
    private String storageKey;



    @Column(
            nullable = false,
            length = 128
    )
    private String checksum;



    @Column(
            name = "mime_type",
            nullable = false,
            length = 150
    )
    private String mimeType;



    @Column(
            name = "size_bytes",
            nullable = false
    )
    private long sizeBytes;



    @Enumerated(EnumType.STRING)
    @Column(
            name = "security_status",
            nullable = false,
            length = 30
    )
    private FileSecurityStatus status;



    @Column(
            name = "quarantine_reason",
            length = 500
    )
    private String quarantineReason;



    @Column(
            name = "reviewed_by"
    )
    private UUID reviewedBy;



    @Column(
            name = "reviewed_at"
    )
    private Instant reviewedAt;



    protected QuarantinedFile() {
    }



    public QuarantinedFile(
            UUID tenantId,
            String storageKey,
            String checksum,
            String mimeType,
            long sizeBytes,
            String reason
    ) {

        setTenantId(
                tenantId
        );

        this.storageKey = storageKey;
        this.checksum = checksum;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.status = FileSecurityStatus.QUARANTINED;
        this.quarantineReason = reason;

    }



    public String storageKey() {
        return storageKey;
    }


    public String checksum() {
        return checksum;
    }


    public String mimeType() {
        return mimeType;
    }


    public long sizeBytes() {
        return sizeBytes;
    }


    public FileSecurityStatus status() {
        return status;
    }


    public String quarantineReason() {
        return quarantineReason;
    }


    public void release(
            UUID reviewer
    ) {

        if (
                status != FileSecurityStatus.QUARANTINED
        ) {

            throw new IllegalStateException(
                    "Only quarantined files can be released"
            );

        }


        this.status =
                FileSecurityStatus.RELEASED;

        this.reviewedBy =
                reviewer;

        this.reviewedAt =
                Instant.now();

    }



    public void reject(
            UUID reviewer,
            String reason
    ) {

        if (
                status != FileSecurityStatus.QUARANTINED
        ) {

            throw new IllegalStateException(
                    "Only quarantined files can be rejected"
            );

        }


        this.status =
                FileSecurityStatus.REJECTED;

        this.reviewedBy =
                reviewer;

        this.reviewedAt =
                Instant.now();


        if (
                reason != null
                &&
                !reason.isBlank()
        ) {

            this.quarantineReason =
                    reason;

        }

    }


}
