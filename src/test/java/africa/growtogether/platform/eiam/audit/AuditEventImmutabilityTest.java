package africa.growtogether.platform.eiam.audit;
import static org.junit.jupiter.api.Assertions.*; import java.time.Instant; import java.util.UUID; import org.junit.jupiter.api.Test;
class AuditEventImmutabilityTest {
 @Test void capturesSecurityContextWithoutMutationApi(){
  AuditEvent event=new AuditEvent(UUID.randomUUID(),UUID.randomUUID(),"john","AUTH.LOGIN.SUCCESS",AuditEventCategory.AUTHENTICATION,AuditOutcome.SUCCESS,SecuritySeverity.INFO,"SESSION","abc","127.0.0.1","JUnit","corr",UUID.randomUUID(),"Login succeeded.","{}",Instant.now());
  assertEquals("AUTH.LOGIN.SUCCESS",event.getEventType()); assertEquals(AuditOutcome.SUCCESS,event.getOutcome()); assertNull(event.getId());
 }
}
