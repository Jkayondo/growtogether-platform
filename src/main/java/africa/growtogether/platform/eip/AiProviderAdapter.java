package africa.growtogether.platform.eip;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.execution.AiTextRequest;
import africa.growtogether.platform.eaif.execution.AiTextResult;

/** AI-specific transport contract; messaging adapters retain their existing API. */
interface AiProviderAdapter {
    String connectorType();
    AiEnums.ProviderType providerType();
    AiTextResult execute(ExternalProviderExecutionContext context, AiTextRequest request);
}
