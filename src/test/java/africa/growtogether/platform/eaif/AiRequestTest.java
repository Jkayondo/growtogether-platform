package africa.growtogether.platform.eaif;
import static org.junit.jupiter.api.Assertions.*; import java.util.UUID; import org.junit.jupiter.api.Test;
class AiRequestTest {
 @Test void followsControlledLifecycle(){AiRequest r=new AiRequest(UUID.randomUUID(),"EDS","DOCUMENT_SUMMARY","MODEL_A","a".repeat(64),AiEnums.RiskLevel.LOW,"corr");r.approve();r.begin();r.succeed("eds://outputs/1");assertEquals(AiEnums.RequestStatus.SUCCEEDED,r.requestStatus());}
 @Test void blocksCriticalWithoutWorkflow(){AiRequest r=new AiRequest(UUID.randomUUID(),"EAP","AUTONOMOUS-DECISION","MODEL_A","b".repeat(64),AiEnums.RiskLevel.CRITICAL,"corr");assertThrows(IllegalArgumentException.class,()->new AiSafetyPolicy().validate(r));}
}
