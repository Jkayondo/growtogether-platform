package africa.growtogether.platform.eds;
import static org.junit.jupiter.api.Assertions.*; import java.time.*; import java.util.*; import org.junit.jupiter.api.Test;
class DocumentLifecycleTest {
 @Test void lifecycleAndRetentionAreControlled(){Document d=new Document(UUID.randomUUID(),"DOC-1","Policy",DocumentClassification.CONFIDENTIAL);d.activateFirstVersion();UUID u=UUID.randomUUID();d.checkOut(u,Instant.now());d.checkIn(u);d.applyRetention(Instant.now().plusSeconds(3600));d.softDelete(Instant.now());assertThrows(IllegalStateException.class,()->d.dispose(Instant.now()));}
 @Test void legalHoldBlocksArchiveAndDelete(){Document d=new Document(UUID.randomUUID(),"DOC-2","Agreement",DocumentClassification.RESTRICTED);d.activateFirstVersion();d.placeLegalHold();assertThrows(IllegalStateException.class,()->d.archive(Instant.now()));assertThrows(IllegalStateException.class,()->d.softDelete(Instant.now()));}
}
