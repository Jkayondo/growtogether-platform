package africa.growtogether.platform.eds;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="eds_document_shares",indexes={@Index(name="ix_eds_share_document",columnList="tenant_id,document_id"),@Index(name="ix_eds_share_token",columnList="token_hash",unique=true)})
public class DocumentShare extends AuditedTenantEntity {
 @Column(name="document_id",nullable=false) private UUID documentId;
 @Column(name="recipient_user_id") private UUID recipientUserId;
 @Column(name="recipient_email",length=320) private String recipientEmail;
 @Enumerated(EnumType.STRING) @Column(name="access_level",nullable=false,length=16) private DocumentAccessLevel accessLevel;
 @Column(name="token_hash",nullable=false,length=64,unique=true) private String tokenHash;
 @Column(name="expires_at",nullable=false) private Instant expiresAt;
 @Column(name="revoked_at") private Instant revokedAt;
 @Column(name="max_downloads") private Integer maxDownloads;
 @Column(name="download_count",nullable=false) private int downloadCount;
 protected DocumentShare(){}
 public DocumentShare(UUID tenantId,UUID documentId,UUID recipientUserId,String recipientEmail,DocumentAccessLevel accessLevel,String tokenHash,Instant expiresAt,Integer maxDownloads){setTenantId(tenantId);this.documentId=documentId;this.recipientUserId=recipientUserId;this.recipientEmail=recipientEmail;this.accessLevel=accessLevel;this.tokenHash=tokenHash;this.expiresAt=expiresAt;this.maxDownloads=maxDownloads;}
 public boolean validAt(Instant now){return revokedAt==null&&expiresAt.isAfter(now)&&(maxDownloads==null||downloadCount<maxDownloads);}
 public void revoke(Instant now){revokedAt=now;}
 public void recordDownload(){if(maxDownloads!=null&&downloadCount>=maxDownloads)throw new IllegalStateException("Share download limit reached");downloadCount++;}
 public UUID id(){return getId();} public UUID documentId(){return documentId;} public UUID recipientUserId(){return recipientUserId;} public String recipientEmail(){return recipientEmail;} public DocumentAccessLevel accessLevel(){return accessLevel;} public Instant expiresAt(){return expiresAt;} public Instant revokedAt(){return revokedAt;} public int downloadCount(){return downloadCount;}
}
