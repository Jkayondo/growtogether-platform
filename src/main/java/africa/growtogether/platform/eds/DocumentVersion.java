package africa.growtogether.platform.eds;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="eds_document_versions",uniqueConstraints={@UniqueConstraint(name="uk_eds_document_version",columnNames={"tenant_id","document_id","version_number"}),@UniqueConstraint(name="uk_eds_storage_key",columnNames={"storage_key"})})
public class DocumentVersion extends AuditedTenantEntity {
 @Column(name="document_id",nullable=false) private UUID documentId;
 @Column(name="version_number",nullable=false) private int versionNumber;
 @Column(name="storage_key",nullable=false,length=500) private String storageKey;
 @Column(nullable=false,length=128) private String checksum;
 @Column(name="mime_type",nullable=false,length=150) private String mimeType;
 @Column(name="size_bytes",nullable=false) private long sizeBytes;
 @Column(name="change_summary",length=500) private String changeSummary;
 @Column(name="immutable",nullable=false) private boolean immutable=true;
 protected DocumentVersion(){}
 public DocumentVersion(UUID tenantId,UUID documentId,int versionNumber,String storageKey,String checksum,String mimeType,long sizeBytes,String summary){setTenantId(tenantId);this.documentId=documentId;this.versionNumber=versionNumber;this.storageKey=storageKey;this.checksum=checksum;this.mimeType=mimeType;this.sizeBytes=sizeBytes;this.changeSummary=summary;}
 public UUID documentId(){return documentId;} public int versionNumber(){return versionNumber;} public String storageKey(){return storageKey;} public String checksum(){return checksum;} public String mimeType(){return mimeType;} public long sizeBytes(){return sizeBytes;} public boolean immutable(){return immutable;}
}
