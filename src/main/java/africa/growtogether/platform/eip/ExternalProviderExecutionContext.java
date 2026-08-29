package africa.growtogether.platform.eip;

import java.time.Duration;
import java.util.UUID;

record ExternalProviderExecutionContext(

        UUID connectorId,

        String connectorCode,

        String connectorType,

        String baseUrl,

        String authType,

        String credential,

        String providerConfiguration,

        Duration requestTimeout

) {
}
