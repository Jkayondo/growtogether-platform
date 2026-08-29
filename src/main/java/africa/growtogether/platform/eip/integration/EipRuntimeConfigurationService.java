package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDefinition;
import africa.growtogether.platform.ecs.ConfigurationDefinitionRepository;
import africa.growtogether.platform.ecs.ConfigurationDtos.PutValue;
import africa.growtogether.platform.ecs.ConfigurationScope;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static africa.growtogether.platform.eip.integration.EipRuntimeConfigurationDtos.*;

@Service
public class EipRuntimeConfigurationService {

    private static final String EXTERNAL_DELIVERY_CODE =
            "EIP_EXTERNAL_DELIVERY_ENABLED";

    private final ConfigurationDefinitionRepository definitions;
    private final ConfigurationService configuration;
    private final EipConfigurationGateway gateway;
    private final EnterpriseIdentityContext identity;

    public EipRuntimeConfigurationService(
            ConfigurationDefinitionRepository definitions,
            ConfigurationService configuration,
            EipConfigurationGateway gateway,
            EnterpriseIdentityContext identity
    ) {
        this.definitions = definitions;
        this.configuration = configuration;
        this.gateway = gateway;
        this.identity = identity;
    }

    @Transactional(readOnly = true)
    public ExternalDeliveryView externalDelivery() {

        UUID tenantId =
                identity.requireTenantId();

        return new ExternalDeliveryView(
                gateway.externalDeliveryEnabled(
                        tenantId
                )
        );
    }

    @Transactional
    public ExternalDeliveryView setExternalDelivery(
            SetExternalDeliveryCommand command
    ) {

        UUID tenantId =
                identity.requireTenantId();

        ConfigurationDefinition definition =
                definitions
                        .findByCodeIgnoreCase(
                                EXTERNAL_DELIVERY_CODE
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "EIP external delivery configuration definition is missing"
                                        )
                        );

        if (!definition.isActive()) {
            throw new IllegalStateException(
                    "EIP external delivery configuration definition is inactive"
            );
        }

        if (
                !definition
                        .getAllowedScopes()
                        .contains(
                                ConfigurationScope.TENANT
                        )
        ) {
            throw new IllegalStateException(
                    "EIP external delivery configuration does not allow tenant scope"
            );
        }

        String reason =
                command.reason() == null
                        || command.reason().isBlank()
                        ? "Governed EIP external delivery runtime control"
                        : command.reason().trim();

        var result =
                configuration.put(
                        definition.getId(),
                        new PutValue(
                                ConfigurationScope.TENANT,
                                null,
                                null,
                                tenantId,
                                Boolean.toString(
                                        command.enabled()
                                ),
                                reason
                        )
                );

        return new ExternalDeliveryView(
                Boolean.parseBoolean(
                        result.value()
                )
        );
    }
}
