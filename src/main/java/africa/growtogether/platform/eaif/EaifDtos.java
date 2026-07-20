package africa.growtogether.platform.eaif;
import jakarta.validation.constraints.*; import java.util.UUID;
record CreateProviderRequest(@NotBlank String code,@NotBlank String name,@NotNull AiEnums.ProviderType type){}
record CreateModelRequest(@NotBlank String code,@NotBlank String providerCode,@NotBlank String providerModel,@NotNull AiEnums.Capability capability){}
record CreatePromptRequest(@NotBlank String code,@Min(1) int version,@NotBlank String userTemplate){}
record CreateAiRequest(@NotBlank String sourceService,@NotBlank String useCase,@NotBlank String modelCode,@NotBlank String inputHash,@NotNull AiEnums.RiskLevel riskLevel,String correlationId){}
record CompleteAiRequest(@NotBlank String outputReference){}
record FailAiRequest(@NotBlank String reason){}
record AiRequestView(UUID id,AiEnums.RequestStatus status,String useCase,String modelCode,AiEnums.RiskLevel riskLevel){static AiRequestView of(AiRequest r){return new AiRequestView(r.getId(),r.requestStatus(),r.useCase(),r.modelCode(),r.riskLevel());}}
