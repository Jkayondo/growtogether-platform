package africa.growtogether.platform.eip;
import static org.junit.jupiter.api.Assertions.*; import java.time.Instant; import java.util.UUID; import org.junit.jupiter.api.Test;
class IntegrationMessageTest {
 private IntegrationMessage message(int max){return new IntegrationMessage(UUID.randomUUID(),"DocumentCreated","1.0","eds","analytics",IntegrationProtocol.INTERNAL_EVENT,"{}",null,"corr","idem",max);}
 @Test void deliversThroughControlledLifecycle(){IntegrationMessage m=message(3);m.route();m.dispatch();m.delivered();assertEquals(IntegrationMessageStatus.DELIVERED,m.messageStatus());}
 @Test void retriesBeforeDeadLetter(){IntegrationMessage m=message(2);m.route();m.dispatch();m.fail("temporary",Instant.now().plusSeconds(5));assertEquals(IntegrationMessageStatus.RETRYING,m.messageStatus());m.dispatch();m.fail("again",Instant.now().plusSeconds(5));assertEquals(IntegrationMessageStatus.DEAD_LETTER,m.messageStatus());}
 @Test void blocksInvalidDispatch(){IntegrationMessage m=message(2);assertThrows(IllegalStateException.class,m::dispatch);}
}
