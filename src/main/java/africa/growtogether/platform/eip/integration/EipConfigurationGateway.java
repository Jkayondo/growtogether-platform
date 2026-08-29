package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Central ECS adapter. EIP never reads ECS persistence tables or hard-codes tenant policy. */
@Component
public class EipConfigurationGateway {
    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;
    public EipConfigurationGateway(ConfigurationService configuration, EnterpriseIdentityContext identity) {
        this.configuration = configuration; this.identity = identity;
    }
    private String value(String code, String fallback) {
        return value(identity.tenantId(), code, fallback);
    }

    private String value(
            UUID tenantId,
            String code,
            String fallback
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        try {
            var resolved =
                    configuration.resolve(
                            new ResolveRequest(
                                    code,
                                    null,
                                    null,
                                    tenantId
                            )
                    );

            return resolved.value() == null
                    ? fallback
                    : resolved.value();

        } catch (RuntimeException ex) {
            return fallback;
        }
    }
    public int maximumAttempts() {
        return maximumAttempts(identity.tenantId());
    }

    public int maximumAttempts(UUID tenantId) {
        try {
            int attempts =
                    Integer.parseInt(
                            value(
                                    tenantId,
                                    "EIP_MAX_ATTEMPTS",
                                    "5"
                            )
                    );

            return attempts > 0 ? attempts : 5;

        } catch (Exception ex) {
            return 5;
        }
    }

    public Duration requestTimeout() {
        return requestTimeout(identity.tenantId());
    }

    public Duration requestTimeout(UUID tenantId) {
        try {
            long seconds =
                    Long.parseLong(
                            value(
                                    tenantId,
                                    "EIP_REQUEST_TIMEOUT_SECONDS",
                                    "30"
                            )
                    );

            return Duration.ofSeconds(
                    seconds > 0 ? seconds : 30
            );

        } catch (Exception ex) {
            return Duration.ofSeconds(30);
        }
    }

    public int circuitFailureThreshold() {
        return circuitFailureThreshold(identity.tenantId());
    }

    public int circuitFailureThreshold(UUID tenantId) {
        try {
            int threshold =
                    Integer.parseInt(
                            value(
                                    tenantId,
                                    "EIP_CIRCUIT_FAILURE_THRESHOLD",
                                    "5"
                            )
                    );

            return threshold > 0 ? threshold : 5;

        } catch (Exception ex) {
            return 5;
        }
    }

    public boolean externalDeliveryEnabled() {
        return externalDeliveryEnabled(identity.tenantId());
    }

    public boolean externalDeliveryEnabled(UUID tenantId) {
        return Boolean.parseBoolean(
                value(
                        tenantId,
                        "EIP_EXTERNAL_DELIVERY_ENABLED",
                        "false"
                )
        );
    }

    public String executionEnvironment() {
        return executionEnvironment(identity.tenantId());
    }

    public String executionEnvironment(UUID tenantId) {
        String environment =
                value(
                        tenantId,
                        "EIP_EXECUTION_ENVIRONMENT",
                        "DEVELOPMENT"
                )
                        .trim()
                        .toUpperCase(java.util.Locale.ROOT);

        return switch (environment) {
            case "DEVELOPMENT",
                 "TEST",
                 "STAGING",
                 "PRODUCTION" -> environment;

            default -> throw new IllegalStateException(
                    "Unsupported EIP execution environment: "
                            + environment
            );
        };
    }
}
