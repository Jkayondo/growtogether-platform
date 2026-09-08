package africa.growtogether.platform.file;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;


import jakarta.persistence.*;

import java.util.UUID;
import java.time.Instant;



@Entity
@Table(
        name = "file_records"
)
public class FileRecord extends AuditedTenantEntity {


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


    @Column(
            name = "owner_type",
            nullable = false,
            length = 50
    )
    private String ownerType;


    @Column(
            name = "owner_id",
            nullable = false
    )
    private UUID ownerId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "security_status",
            nullable = false,
            length = 30
    )
    private FileSecurityStatus status;


    @Column(
            name = "retention_until"
    )
    private Instant retentionUntil;


    @Column(
            name = "legal_hold",
            nullable = false
    )
    private boolean legalHold;


    protected FileRecord() {
    }



    public FileRecord(
            UUID tenantId,
            String storageKey,
            String checksum,
            String mimeType,
            long sizeBytes,
            String ownerType,
            UUID ownerId
    ) {

        setTenantId(
                tenantId
        );

        this.storageKey = storageKey;
        this.checksum = checksum;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.status = FileSecurityStatus.CLEAN;
        this.legalHold = false;

    }



    public void applyRetention(
            Instant until
    ) {

        this.retentionUntil = until;

    }



    public void placeLegalHold() {

        this.legalHold = true;

    }



    public void releaseLegalHold() {

        this.legalHold = false;

    }



    public void validateDisposal(
            Instant now
    ) {


        if (legalHold) {

            throw new IllegalStateException(
                    "File is under legal hold"
            );

        }


        if (
                retentionUntil != null
                &&
                retentionUntil.isAfter(now)
        ) {

            throw new IllegalStateException(
                    "Retention period has not expired"
            );

        }

    }



    public Instant retentionUntil() {

        return retentionUntil;

    }



    public boolean legalHold() {

        return legalHold;

    }




    public String storageKey() {
        return storageKey;
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

}
