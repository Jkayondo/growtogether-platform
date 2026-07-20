package africa.growtogether.platform.eap;
import org.junit.jupiter.api.Test; import java.time.Instant; import static org.junit.jupiter.api.Assertions.*;
class AnalyticsRuntimeDomainTest {
 @Test void eventLifecycleProcessesSuccessfully(){AnalyticsEvent e=new AnalyticsEvent(java.util.UUID.randomUUID(),"TEST","unit",null,Instant.now(),"{}");e.begin();e.complete();assertEquals(AnalyticsEnums.EventStatus.PROCESSED,e.processingStatus());}
 @Test void eventMovesToDeadLetterAtMaximumAttempts(){AnalyticsEvent e=new AnalyticsEvent(java.util.UUID.randomUUID(),"TEST","unit",null,Instant.now(),"{}");e.begin();e.fail("bad",1);assertEquals(AnalyticsEnums.EventStatus.DEAD_LETTER,e.processingStatus());}
 @Test void reportLifecycleRequiresRunningState(){ReportExecution r=new ReportExecution(java.util.UUID.randomUUID(),"REPORT",null,AnalyticsEnums.ExportFormat.CSV);assertThrows(IllegalStateException.class,()->r.complete("x"));r.start();r.complete("artifact.csv");assertEquals(AnalyticsEnums.ReportStatus.COMPLETED,r.reportStatus());}
}
