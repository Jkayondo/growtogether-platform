package africa.growtogether.platform.ens.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EnsConfigurationGateway {

    private final ConfigurationService configuration;
    private final EnterpriseIdentityContext identity;

    public EnsConfigurationGateway(
            ConfigurationService configuration,
            EnterpriseIdentityContext identity
    ) {
        this.configuration = configuration;
        this.identity = identity;
    }

    /*
     * Legacy interactive path. Existing callers retain principal-derived
     * tenant behaviour.
     */
    public int maxAttempts() {
        return maxAttempts(
                identity.tenantId()
        );
    }

    public long retryBackoffSeconds(
            int attempt
    ) {
        return retryBackoffSeconds(
                identity.tenantId(),
                attempt
        );
    }

    /*
     * Internal/worker path. The dispatcher must resolve configuration
     * explicitly for the notification tenant rather than depend on an
     * authenticated HTTP principal.
     */
    public int maxAttempts(
            UUID tenantId
    ) {
        try {
            int configured =
                    Integer.parseInt(
                            value(
                                    tenantId,
                                    "ENS_MAX_RETRY_ATTEMPTS",
                                    "5"
                            )
                    );

            return configured > 0
                    ? configured
                    : 5;

        } catch (RuntimeException exception) {
            return 5;
        }
    }

    public long retryBackoffSeconds(
            UUID tenantId,
            int attempt
    ) {
        long base;

        try {
            base =
                    Long.parseLong(
                            value(
                                    tenantId,
                                    "ENS_RETRY_BASE_SECONDS",
                                    "30"
                            )
                    );

            if (base <= 0) {
                base = 30;
            }

        } catch (RuntimeException exception) {
            base = 30;
        }

        return Math.min(
                base
                        * (
                                1L
                                << Math.min(
                                        Math.max(
                                                attempt - 1,
                                                0
                                        ),
                                        10
                                )
                        ),
                86400
        );
    }

    private String value(
            UUID tenantId,
            String code,
            String fallback
    ) {
        if (tenantId == null) {
            return fallback;
        }

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

        } catch (RuntimeException exception) {
            return fallback;
        }
    }
}
