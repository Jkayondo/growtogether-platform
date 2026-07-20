package africa.growtogether.platform.ewe;
import static org.junit.jupiter.api.Assertions.*; import java.util.UUID; import org.junit.jupiter.api.Test;
class WorkflowInstanceTest {
 @Test void supportsExecutionLifecycle(){WorkflowInstance i=new WorkflowInstance(UUID.randomUUID(),1,"ADMISSION-1","{}",null);i.start("REVIEW");assertEquals(WorkflowInstanceStatus.RUNNING,i.getInstanceStatus());i.waitAt("APPROVAL");assertEquals(WorkflowInstanceStatus.WAITING,i.getInstanceStatus());i.resume();i.advance("FINALIZE","{\"approved\":true}");i.complete();assertEquals(WorkflowInstanceStatus.COMPLETED,i.getInstanceStatus());assertNotNull(i.getCompletedAt());}
 @Test void failedInstanceCanRetry(){WorkflowInstance i=new WorkflowInstance(UUID.randomUUID(),1,null,"{}",null);i.start("PAYMENT");i.fail("Provider unavailable");i.retry();assertEquals(WorkflowInstanceStatus.RUNNING,i.getInstanceStatus());assertEquals(1,i.getRetryCount());}
 @Test void terminalInstanceCannotCancel(){WorkflowInstance i=new WorkflowInstance(UUID.randomUUID(),1,null,"{}",null);i.start("DONE");i.complete();assertThrows(WorkflowException.class,i::cancel);}
}
