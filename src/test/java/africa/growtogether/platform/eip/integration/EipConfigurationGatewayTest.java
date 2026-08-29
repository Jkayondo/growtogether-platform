package africa.growtogether.platform.eip.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDataType;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolvedValue;
import africa.growtogether.platform.ecs.ConfigurationScope;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EipConfigurationGatewayTest {

    private ConfigurationService configuration;
    private EnterpriseIdentityContext identity;
    private EipConfigurationGateway gateway;

    @BeforeEach
    void setUp() {
        configuration = mock(ConfigurationService.class);
        identity = mock(EnterpriseIdentityContext.class);

        gateway =
                new EipConfigurationGateway(
                        configuration,
                        identity
                );
    }

    @Test
    void explicitTenantIsPassedToEcs() {
        UUID tenantId = UUID.randomUUID();

        when(configuration.resolve(any(ResolveRequest.class)))
                .thenReturn(
                        resolved(
                                "EIP_EXTERNAL_DELIVERY_ENABLED",
                                ConfigurationDataType.BOOLEAN,
                                "true"
                        )
                );

        assertThat(
                gateway.externalDeliveryEnabled(tenantId)
        ).isTrue();

        ArgumentCaptor<ResolveRequest> request =
                ArgumentCaptor.forClass(
                        ResolveRequest.class
                );

        verify(configuration)
                .resolve(request.capture());

        assertThat(request.getValue().code())
                .isEqualTo(
                        "EIP_EXTERNAL_DELIVERY_ENABLED"
                );

        assertThat(request.getValue().tenantId())
                .isEqualTo(tenantId);
    }

    @Test
    void legacyNoArgumentMethodUsesIdentityTenant() {
        UUID tenantId = UUID.randomUUID();

        when(identity.tenantId())
                .thenReturn(tenantId);

        when(configuration.resolve(any(ResolveRequest.class)))
                .thenReturn(
                        resolved(
                                "EIP_EXECUTION_ENVIRONMENT",
                                ConfigurationDataType.ENUM,
                                "staging"
                        )
                );

        assertThat(
                gateway.executionEnvironment()
        ).isEqualTo("STAGING");

        ArgumentCaptor<ResolveRequest> request =
                ArgumentCaptor.forClass(
                        ResolveRequest.class
                );

        verify(configuration)
                .resolve(request.capture());

        assertThat(request.getValue().tenantId())
                .isEqualTo(tenantId);
    }

    @Test
    void externalDeliveryFallsBackToDisabledWhenEcsFails() {
        UUID tenantId = UUID.randomUUID();

        when(configuration.resolve(any(ResolveRequest.class)))
                .thenThrow(
                        new IllegalStateException(
                                "ECS unavailable"
                        )
                );

        assertThat(
                gateway.externalDeliveryEnabled(tenantId)
        ).isFalse();
    }

    @Test
    void nonPositiveNumericValuesUseSafeDefaults() {
        UUID tenantId = UUID.randomUUID();

        when(configuration.resolve(any(ResolveRequest.class)))
                .thenAnswer(invocation -> {
                    ResolveRequest request =
                            invocation.getArgument(0);

                    return switch (request.code()) {
                        case "EIP_MAX_ATTEMPTS" ->
                                resolved(
                                        request.code(),
                                        ConfigurationDataType.INTEGER,
                                        "0"
                                );

                        case "EIP_REQUEST_TIMEOUT_SECONDS" ->
                                resolved(
                                        request.code(),
                                        ConfigurationDataType.INTEGER,
                                        "-20"
                                );

                        case "EIP_CIRCUIT_FAILURE_THRESHOLD" ->
                                resolved(
                                        request.code(),
                                        ConfigurationDataType.INTEGER,
                                        "0"
                                );

                        default ->
                                throw new IllegalArgumentException(
                                        "Unexpected code: "
                                                + request.code()
                                );
                    };
                });

        assertThat(
                gateway.maximumAttempts(tenantId)
        ).isEqualTo(5);

        assertThat(
                gateway.requestTimeout(tenantId)
        ).isEqualTo(Duration.ofSeconds(30));

        assertThat(
                gateway.circuitFailureThreshold(tenantId)
        ).isEqualTo(5);
    }

    @Test
    void executionEnvironmentIsNormalized() {
        UUID tenantId = UUID.randomUUID();

        when(configuration.resolve(any(ResolveRequest.class)))
                .thenReturn(
                        resolved(
                                "EIP_EXECUTION_ENVIRONMENT",
                                ConfigurationDataType.ENUM,
                                "  production  "
                        )
                );

        assertThat(
                gateway.executionEnvironment(tenantId)
        ).isEqualTo("PRODUCTION");
    }

    @Test
    void unsupportedExecutionEnvironmentFailsClosed() {
        UUID tenantId = UUID.randomUUID();

        when(configuration.resolve(any(ResolveRequest.class)))
                .thenReturn(
                        resolved(
                                "EIP_EXECUTION_ENVIRONMENT",
                                ConfigurationDataType.ENUM,
                                "SANDBOX"
                        )
                );

        assertThatThrownBy(
                () -> gateway.executionEnvironment(
                        tenantId
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Unsupported EIP execution environment: SANDBOX"
                );
    }

    private ResolvedValue resolved(
            String code,
            ConfigurationDataType dataType,
            String value
    ) {
        return new ResolvedValue(
                code,
                dataType,
                value,
                ConfigurationScope.TENANT,
                1L,
                false
        );
    }
}
