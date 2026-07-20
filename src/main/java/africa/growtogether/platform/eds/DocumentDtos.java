package africa.growtogether.platform.eds;
import java.time.Instant; import java.util.UUID;
public final class DocumentDtos { private DocumentDtos(){}
 public record CreateDocument(String documentNumber,String title,DocumentClassification classification,String storageKey,String checksum,String mimeType,long sizeBytes,String changeSummary){}
 public record AddVersion(String storageKey,String checksum,String mimeType,long sizeBytes,String changeSummary){}
 public record RetentionRequest(Instant retentionUntil){}
 public record LifecycleView(UUID id,String documentNumber,String title,DocumentStatus status,int currentVersion,Instant retentionUntil,boolean legalHold,UUID checkedOutBy){}
}
