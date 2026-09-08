package africa.growtogether.platform.eaif.execution;

public record AiTextResult(String providerResponseId, String text) {
    public AiTextResult {
        if (providerResponseId == null || providerResponseId.isBlank() || providerResponseId.length() > 200)
            throw new IllegalArgumentException("Invalid provider response identifier");
        if (text == null || text.isBlank() || text.length() > 1000000)
            throw new IllegalArgumentException("Invalid provider text output");
    }
    @Override public String toString() { return "AiTextResult[content redacted]"; }
}
