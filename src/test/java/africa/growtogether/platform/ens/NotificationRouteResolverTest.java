package africa.growtogether.platform.ens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.eip.ExternalConnectorReadGateway;
import africa.growtogether.platform.eip.ExternalConnectorSnapshot;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRouteResolverTest {

    @Mock
    private NotificationChannelRouteRepository routes;

    @Mock
    private ExternalConnectorReadGateway connectors;

    private NotificationRouteResolver resolver;

    @BeforeEach
    void setUp() {
        resolver =
                new NotificationRouteResolver(
                        routes,
                        connectors
                );
    }

    @Test
    void resolvesUsableRoutesInConfiguredPriorityOrder() {

        UUID tenantId = UUID.randomUUID();

        UUID connectorOneId = UUID.randomUUID();
        UUID connectorTwoId = UUID.randomUUID();

        NotificationChannelRoute primary =
                new NotificationChannelRoute(
                        tenantId,
                        NotificationChannel.WHATSAPP,
                        connectorOneId,
                        1
                );

        NotificationChannelRoute secondary =
                new NotificationChannelRoute(
                        tenantId,
                        NotificationChannel.WHATSAPP,
                        connectorTwoId,
                        2
                );

        when(
                routes
                        .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                                tenantId,
                                NotificationChannel.WHATSAPP,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        primary,
                        secondary
                )
        );

        when(
                connectors.require(
                        tenantId,
                        connectorOneId
                )
        ).thenReturn(
                new ExternalConnectorSnapshot(
                        connectorOneId,
                        "WHATSAPP-A",
                        "WHATSAPP",
                        true,
                        "ACTIVE"
                )
        );

        when(
                connectors.require(
                        tenantId,
                        connectorTwoId
                )
        ).thenReturn(
                new ExternalConnectorSnapshot(
                        connectorTwoId,
                        "WHATSAPP-B",
                        "WHATSAPP",
                        true,
                        "ACTIVE"
                )
        );

        List<NotificationRouteCandidate> result =
                resolver.resolve(
                        tenantId,
                        NotificationChannel.WHATSAPP
                );

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).priority());
        assertEquals("WHATSAPP-A", result.get(0).connectorCode());
        assertEquals(2, result.get(1).priority());
        assertEquals("WHATSAPP-B", result.get(1).connectorCode());

        verify(routes)
                .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                        tenantId,
                        NotificationChannel.WHATSAPP,
                        EntityStatus.ACTIVE
                );
    }

    @Test
    void excludesInactiveConnector() {

        UUID tenantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();

        NotificationChannelRoute route =
                new NotificationChannelRoute(
                        tenantId,
                        NotificationChannel.SMS,
                        connectorId,
                        1
                );

        when(
                routes
                        .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                                tenantId,
                                NotificationChannel.SMS,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(route)
        );

        when(
                connectors.require(
                        tenantId,
                        connectorId
                )
        ).thenReturn(
                new ExternalConnectorSnapshot(
                        connectorId,
                        "SMS-A",
                        "SMS",
                        false,
                        "ACTIVE"
                )
        );

        assertEquals(
                0,
                resolver
                        .resolve(
                                tenantId,
                                NotificationChannel.SMS
                        )
                        .size()
        );
    }

    @Test
    void excludesConnectorThatCannotBeResolvedForTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();

        NotificationChannelRoute route =
                new NotificationChannelRoute(
                        tenantId,
                        NotificationChannel.EMAIL,
                        connectorId,
                        1
                );

        when(
                routes
                        .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                                tenantId,
                                NotificationChannel.EMAIL,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(route)
        );

        when(
                connectors.require(
                        tenantId,
                        connectorId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "External connector not found for tenant"
                )
        );

        assertEquals(
                0,
                resolver
                        .resolve(
                                tenantId,
                                NotificationChannel.EMAIL
                        )
                        .size()
        );
    }

    @Test
    void requirePrimaryReturnsFirstUsableRoute() {

        UUID tenantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();

        NotificationChannelRoute route =
                new NotificationChannelRoute(
                        tenantId,
                        NotificationChannel.WHATSAPP,
                        connectorId,
                        1
                );

        when(
                routes
                        .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                                tenantId,
                                NotificationChannel.WHATSAPP,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(route)
        );

        when(
                connectors.require(
                        tenantId,
                        connectorId
                )
        ).thenReturn(
                new ExternalConnectorSnapshot(
                        connectorId,
                        "WHATSAPP-PRIMARY",
                        "WHATSAPP",
                        true,
                        "ACTIVE"
                )
        );

        NotificationRouteCandidate result =
                resolver.requirePrimary(
                        tenantId,
                        NotificationChannel.WHATSAPP
                );

        assertEquals(
                "WHATSAPP-PRIMARY",
                result.connectorCode()
        );

        assertEquals(
                1,
                result.priority()
        );
    }

    @Test
    void requirePrimaryFailsWhenNoUsableRouteExists() {

        UUID tenantId = UUID.randomUUID();

        when(
                routes
                        .findByTenantIdAndChannelAndEnabledTrueAndStatusOrderByPriorityAsc(
                                tenantId,
                                NotificationChannel.WHATSAPP,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> resolver.requirePrimary(
                                tenantId,
                                NotificationChannel.WHATSAPP
                        )
                );

        assertEquals(
                "No usable notification provider route configured",
                exception.getMessage()
        );
    }
}
