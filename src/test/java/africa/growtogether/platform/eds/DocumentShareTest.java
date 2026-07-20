package africa.growtogether.platform.eds;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*; import java.util.*;
import org.junit.jupiter.api.Test;
class DocumentShareTest {
 @Test void expiresAndRevokesSecurely(){DocumentShare s=new DocumentShare(UUID.randomUUID(),UUID.randomUUID(),null,"user@example.com",DocumentAccessLevel.READ,"a".repeat(64),Instant.now().plusSeconds(60),1);assertTrue(s.validAt(Instant.now()));s.recordDownload();assertFalse(s.validAt(Instant.now()));}
 @Test void revokedShareIsInvalid(){DocumentShare s=new DocumentShare(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),null,DocumentAccessLevel.READ,"b".repeat(64),Instant.now().plusSeconds(60),null);s.revoke(Instant.now());assertFalse(s.validAt(Instant.now()));}
}
