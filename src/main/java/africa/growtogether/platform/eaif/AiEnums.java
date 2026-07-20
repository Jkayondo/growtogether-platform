package africa.growtogether.platform.eaif;
public final class AiEnums {
 private AiEnums() {}
 public enum ProviderType { OPENAI_COMPATIBLE, AZURE_OPENAI, ANTHROPIC, GOOGLE, LOCAL, CUSTOM }
 public enum Capability { CHAT, COMPLETION, EMBEDDING, OCR, CLASSIFICATION, SUMMARIZATION, TRANSLATION, EXTRACTION, MODERATION }
 public enum RequestStatus { RECEIVED, VALIDATING, APPROVED, PROCESSING, SUCCEEDED, FAILED, REJECTED, CANCELLED }
 public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
}
