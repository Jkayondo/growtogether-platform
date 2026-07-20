package africa.growtogether.platform.eap.advanced;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.util.UUID; import static org.junit.jupiter.api.Assertions.*;
class AlertRuleTest { @Test void evaluatesThreshold(){AlertRule r=new AlertRule(UUID.randomUUID(),"high_failures","High failures","FAILURES",AdvancedAnalyticsEnums.AlertOperator.GREATER_THAN,new BigDecimal("10"),AdvancedAnalyticsEnums.AlertSeverity.HIGH,60);assertTrue(r.matches(new BigDecimal("11")));assertFalse(r.matches(new BigDecimal("10")));}}
