package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ecs.ConfigurationDataType;
import africa.growtogether.platform.ecs.ConfigurationDefinition;
import africa.growtogether.platform.ecs.ConfigurationDefinitionRepository;
import africa.growtogether.platform.ecs.ConfigurationDtos.PutValue;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolvedValue;
import africa.growtogether.platform.ecs.ConfigurationScope;
import africa.growtogether.platform.ecs.ConfigurationService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static africa.growtogether.platform.eip.integration.EipRuntimeConfigurationDtos.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EipRuntimeConfigurationServiceTest {

    @Mock
    private ConfigurationDefinitionRepository definitions;

    @Mock
    private ConfigurationService configuration;

    @Mock
    private EipConfigurationGateway gateway;

    @Mock
    private EnterpriseIdentityContext identity;

    private EipRuntimeConfigurationService service;

    @BeforeEach
    void setUp() {
        service =
                new EipRuntimeConfigurationService(
                        definitions,
                        configuration,
                        gateway,
                        identity
                );
    }

    @Test
    void writesOnlyAuthenticatedTenantScope() {

        UUID tenantId =
                UUID.randomUUID();

        UUID definitionId =
                UUID.randomUUID();

        ConfigurationDefinition definition =
                org.mockito.Mockito.mock(
                        ConfigurationDefinition.class
                );

        when(identity.requireTenantId())
                .thenReturn(tenantId);

        when(
                definitions.findByCodeIgnoreCase(
                        "EIP_EXTERNAL_DELIVERY_ENABLED"
                )
        ).thenReturn(
                Optional.of(definition)
        );

        when(definition.isActive())
                .thenReturn(true);

        when(definition.getAllowedScopes())
                .thenReturn(
                        Set.of(
                                ConfigurationScope.PLATFORM,
                                ConfigurationScope.TENANT
                        )
                );

        when(definition.getId())
                .thenReturn(definitionId);

        when(
                configuration.put(
                        org.mockito.ArgumentMatchers.eq(
                                definitionId
                        ),
                        any(PutValue.class)
                )
        ).thenReturn(
                new ResolvedValue(
                        "EIP_EXTERNAL_DELIVERY_ENABLED",
                        ConfigurationDataType.BOOLEAN,
                        "true",
                        ConfigurationScope.TENANT,
                        1L,
                        false
                )
        );

        ExternalDeliveryView result =
                service.setExternalDelivery(
                        new SetExternalDeliveryCommand(
                                true,
                                "B8 live EMAIL validation"
                        )
                );

        assertThat(result.enabled())
                .isTrue();

        ArgumentCaptor<PutValue> value =
                ArgumentCaptor.forClass(
                        PutValue.class
                );

        verify(configuration)
                .put(
                        org.mockito.ArgumentMatchers.eq(
                                definitionId
                        ),
                        value.capture()
                );

        assertThat(
                value.getValue().scope()
        ).isEqualTo(
                ConfigurationScope.TENANT
        );

        assertThat(
                value.getValue().tenantId()
        ).isEqualTo(
                tenantId
        );

        assertThat(
                value.getValue().organizationId()
        ).isNull();

        assertThat(
                value.getValue().countryCode()
        ).isNull();

        assertThat(
                value.getValue().value()
        ).isEqualTo(
                "true"
        );
    }

    @Test
    void readsAuthenticatedTenantThroughFailClosedGateway() {

        UUID tenantId =
                UUID.randomUUID();

        when(identity.requireTenantId())
                .thenReturn(tenantId);

        when(
                gateway.externalDeliveryEnabled(
                        tenantId
                )
        ).thenReturn(false);

        assertThat(
                service.externalDelivery().enabled()
        ).isFalse();

        verify(gateway)
                .externalDeliveryEnabled(
                        tenantId
                );
    }

    @Test
    void missingDefinitionCannotEnableExternalDelivery() {

        UUID tenantId =
                UUID.randomUUID();

        when(identity.requireTenantId())
                .thenReturn(tenantId);

        when(
                definitions.findByCodeIgnoreCase(
                        "EIP_EXTERNAL_DELIVERY_ENABLED"
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.setExternalDelivery(
                                new SetExternalDeliveryCommand(
                                        true,
                                        "test"
                                )
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "definition is missing"
                );

        verify(
                configuration,
                never()
        ).put(
                any(),
                any()
        );
    }
}
