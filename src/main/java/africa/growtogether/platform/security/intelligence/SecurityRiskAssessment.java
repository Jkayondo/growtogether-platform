package africa.growtogether.platform.security.intelligence;

public record SecurityRiskAssessment(
        int riskScore,
        RiskLevel riskLevel,
        String reason
) {

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
