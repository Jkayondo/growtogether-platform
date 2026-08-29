package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eip.ExternalConnectorReadGateway;
import africa.growtogether.platform.eip.ExternalConnectorSnapshot;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static africa.growtogether.platform.ens.NotificationRouteAdministrationDtos.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRouteAdministrationServiceTest {

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private NotificationChannelRouteRepository routes;

    @Mock
    private ExternalConnectorReadGateway connectors;

    private NotificationRouteAdministrationService service;

    private UUID tenantId;
    private UUID connectorId;

    @BeforeEach
    void setUp() {
        service =
                new NotificationRouteAdministrationService(
                        identity,
                        routes,
                        connectors
                );

        tenantId = UUID.randomUUID();
        connectorId = UUID.randomUUID();

        when(identity.tenantId())
                .thenReturn(tenantId);
    }

    @Test
    void createsTenantScopedRouteThroughAuthoritativeConnectorBoundary() {

        when(
                routes.findByTenantIdAndChannelAndConnectorId(
                        tenantId,
                        NotificationChannel.EMAIL,
                        connectorId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        when(
                routes.existsByTenantIdAndChannelAndPriority(
                        tenantId,
                        NotificationChannel.EMAIL,
                        1
                )
        ).thenReturn(false);

        when(
                connectors.require(
                        tenantId,
                        connectorId
                )
        ).thenReturn(
                new ExternalConnectorSnapshot(
                        connectorId,
                        "BREVO_PRIMARY_EMAIL",
                        "BREVO_EMAIL",
                        true,
                        "ACTIVE"
                )
        );

        when(routes.save(any(NotificationChannelRoute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RouteView view =
                service.create(
                        new CreateRouteCommand(
                                NotificationChannel.EMAIL,
                                connectorId,
                                1,
                                false
                        )
                );

        assertThat(view.channel())
                .isEqualTo(NotificationChannel.EMAIL);

        assertThat(view.connectorId())
                .isEqualTo(connectorId);

        assertThat(view.priority())
                .isEqualTo(1);

        assertThat(view.failoverEnabled())
                .isFalse();

        ArgumentCaptor<NotificationChannelRoute> routeCaptor =
                ArgumentCaptor.forClass(
                        NotificationChannelRoute.class
                );

        verify(routes).save(
                routeCaptor.capture()
        );

        assertThat(
                routeCaptor.getValue().getTenantId()
        ).isEqualTo(tenantId);

        verify(connectors).require(
                tenantId,
                connectorId
        );
    }

    @Test
    void rejectsDuplicateConnectorRouteBeforePersistence() {

        when(
                routes.findByTenantIdAndChannelAndConnectorId(
                        tenantId,
                        NotificationChannel.EMAIL,
                        connectorId
                )
        ).thenReturn(
                java.util.Optional.of(
                        new NotificationChannelRoute(
                                tenantId,
                                NotificationChannel.EMAIL,
                                connectorId,
                                1
                        )
                )
        );

        assertThatThrownBy(
                () -> service.create(
                        new CreateRouteCommand(
                                NotificationChannel.EMAIL,
                                connectorId,
                                1,
                                true
                        )
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(routes, never())
                .save(any());
    }

    @Test
    void rejectsDuplicateChannelPriorityBeforePersistence() {

        when(
                routes.findByTenantIdAndChannelAndConnectorId(
                        tenantId,
                        NotificationChannel.EMAIL,
                        connectorId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        when(
                routes.existsByTenantIdAndChannelAndPriority(
                        tenantId,
                        NotificationChannel.EMAIL,
                        1
                )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.create(
                        new CreateRouteCommand(
                                NotificationChannel.EMAIL,
                                connectorId,
                                1,
                                true
                        )
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("priority");

        verify(routes, never())
                .save(any());
    }

    @Test
    void rejectsInactiveConnector() {

        when(
                routes.findByTenantIdAndChannelAndConnectorId(
                        tenantId,
                        NotificationChannel.EMAIL,
                        connectorId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        when(
                routes.existsByTenantIdAndChannelAndPriority(
                        tenantId,
                        NotificationChannel.EMAIL,
                        1
                )
        ).thenReturn(false);

        when(
                connectors.require(
                        tenantId,
                        connectorId
                )
        ).thenReturn(
                new ExternalConnectorSnapshot(
                        connectorId,
                        "BREVO_PRIMARY_EMAIL",
                        "BREVO_EMAIL",
                        false,
                        "ACTIVE"
                )
        );

        assertThatThrownBy(
                () -> service.create(
                        new CreateRouteCommand(
                                NotificationChannel.EMAIL,
                                connectorId,
                                1,
                                true
                        )
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active and usable");

        verify(routes, never())
                .save(any());
    }

    @Test
    void listsOnlyAuthenticatedTenantRoutes() {

        NotificationChannelRoute route =
                new NotificationChannelRoute(
                        tenantId,
                        NotificationChannel.EMAIL,
                        connectorId,
                        1
                );

        when(
                routes.findByTenantIdOrderByChannelAscPriorityAsc(
                        tenantId
                )
        ).thenReturn(
                List.of(route)
        );

        List<RouteView> result =
                service.list();

        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).connectorId())
                .isEqualTo(connectorId);

        verify(routes)
                .findByTenantIdOrderByChannelAscPriorityAsc(
                        tenantId
                );
    }
}
