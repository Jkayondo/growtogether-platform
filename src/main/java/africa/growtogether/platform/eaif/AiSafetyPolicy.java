package africa.growtogether.platform.eaif;
import org.springframework.stereotype.Component;
@Component
public class AiSafetyPolicy {
 public void validate(AiRequest request){
  if(request.riskLevel()==AiEnums.RiskLevel.CRITICAL) throw new IllegalArgumentException("Critical-risk AI requests require an external approval workflow.");
  if(request.useCase().toLowerCase().contains("autonomous-decision")) throw new IllegalArgumentException("Autonomous high-impact decisions are not permitted.");
 }
}
