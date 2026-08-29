package africa.growtogether.platform.eip;

import java.util.UUID;

public record ExternalProviderExecutionAuthorization(

        UUID connectorId,

        String connectorCode,

        String connectorType,

        String executionEnvironment,

        UUID certificationId

) {
}
