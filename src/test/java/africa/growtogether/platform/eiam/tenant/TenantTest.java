package africa.growtogether.platform.eiam.tenant;
import static org.junit.jupiter.api.Assertions.*; import java.util.UUID; import org.junit.jupiter.api.Test;
class TenantTest {
 @Test void activatesAndSuspends(){Tenant t=new Tenant(UUID.randomUUID(),"school-one","School One");assertEquals(TenantStatus.PROVISIONING,t.getStatus());t.activate();t.suspend();assertEquals(TenantStatus.SUSPENDED,t.getStatus());}
 @Test void deactivationIsTerminal(){Tenant t=new Tenant(UUID.randomUUID(),"school-one","School One");t.activate();t.deactivate();assertThrows(TenantLifecycleException.class,t::activate);}
}
