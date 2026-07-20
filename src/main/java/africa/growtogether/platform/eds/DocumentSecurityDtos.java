package africa.growtogether.platform.eds;
import java.time.Instant;
import java.util.*;
public final class DocumentSecurityDtos { private DocumentSecurityDtos(){}
 public record ShareRequest(UUID recipientUserId,String recipientEmail,DocumentAccessLevel accessLevel,Instant expiresAt,Integer maxDownloads){}
 public record ShareView(UUID id,UUID documentId,UUID recipientUserId,String recipientEmail,DocumentAccessLevel accessLevel,Instant expiresAt,Instant revokedAt,int downloadCount,String token){}
 public record SearchResult(UUID id,String documentNumber,String title,DocumentStatus status,DocumentClassification classification,int currentVersion){}
 public record Preview(UUID documentId,int version,String mimeType,long sizeBytes,String storageKey,boolean inlineSupported,String disposition){}
 public record ClassificationRequest(DocumentClassification classification){}
}
