package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.connect.integration.ConnectInternalMessageGateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectInternalMessageGatewayTest {

    @Mock
    private ConnectSpaceRepository spaces;

    @Mock
    private ConnectMessageRepository messages;

    private ConnectInternalMessageGateway gateway;

    @BeforeEach
    void setUp() {

        RequestContextHolder.clear();

        gateway =
                new ConnectInternalMessageGateway(
                        spaces,
                        messages
                );
    }

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void sendsAutomatedSystemMessageWithRequiredProvenance() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                spaceId
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectMessage result =
                gateway.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "School calendar reminder.",
                        "GT-SCHOOL",
                        "calendar-event-001",
                        "corr-001"
                );

        assertNull(
                result.getSenderUserId()
        );

        assertEquals(
                ConnectMessageType.SYSTEM,
                result.getMessageType()
        );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                spaceId,
                result.getSpaceId()
        );

        assertEquals(
                "School calendar reminder.",
                result.getBody()
        );

        assertEquals(
                "GT-SCHOOL",
                result.getSourceService()
        );

        assertEquals(
                "calendar-event-001",
                result.getSourceReference()
        );

        verify(
                spaces
        ).findByIdAndTenantId(
                spaceId,
                tenantId
        );

        verify(
                messages
        ).saveAndFlush(
                any(ConnectMessage.class)
        );
    }

    @Test
    void createsTemporaryBackgroundContextAndClearsItAfterDelivery() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                spaceId
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation -> {

                    RequestContext active =
                            RequestContextHolder.require();

                    assertEquals(
                            tenantId.toString(),
                            active.tenantId()
                    );

                    assertEquals(
                            "background-correlation",
                            active.correlationId()
                    );

                    return invocation.getArgument(0);
                }
        );

        assertTrue(
                RequestContextHolder.current()
                        .isEmpty()
        );

        gateway.sendSystemMessage(
                tenantId,
                spaceId,
                "Automated message",
                "EWE",
                "workflow-001",
                "background-correlation"
        );

        assertTrue(
                RequestContextHolder.current()
                        .isEmpty()
        );
    }

    @Test
    void existingCompatibleContextIsRestoredAfterDelivery() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContext previous =
                new RequestContext(
                        "original-correlation",
                        tenantId.toString()
                );

        RequestContextHolder.set(
                previous
        );

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                spaceId
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation -> {

                    RequestContext active =
                            RequestContextHolder.require();

                    assertEquals(
                            "gateway-correlation",
                            active.correlationId()
                    );

                    assertEquals(
                            tenantId.toString(),
                            active.tenantId()
                    );

                    return invocation.getArgument(0);
                }
        );

        gateway.sendSystemMessage(
                tenantId,
                spaceId,
                "Automated message",
                "GT-SCHOOL",
                "event-001",
                "gateway-correlation"
        );

        RequestContext restored =
                RequestContextHolder.require();

        assertEquals(
                "original-correlation",
                restored.correlationId()
        );

        assertEquals(
                tenantId.toString(),
                restored.tenantId()
        );
    }

    @Test
    void existingCorrelationIsReusedWhenCallerDoesNotSupplyOne() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "existing-correlation",
                        tenantId.toString()
                )
        );

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                spaceId
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenAnswer(
                invocation -> {

                    assertEquals(
                            "existing-correlation",
                            RequestContextHolder
                                    .require()
                                    .correlationId()
                    );

                    return invocation.getArgument(0);
                }
        );

        gateway.sendSystemMessage(
                tenantId,
                spaceId,
                "Automated message",
                "GT-SCHOOL",
                "event-002",
                null
        );
    }

    @Test
    void crossTenantActiveContextIsRejectedBeforeRepositoryAccess() {

        UUID activeTenant =
                UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "cross-tenant-test",
                        activeTenant.toString()
                )
        );

        assertThrows(
                TenantScopeViolationException.class,
                () -> gateway.sendSystemMessage(
                        hostileTenant,
                        spaceId,
                        "Cross tenant attempt",
                        "GT-SCHOOL",
                        "event-hostile",
                        "cross-tenant-test"
                )
        );

        verifyNoInteractions(
                spaces,
                messages
        );

        RequestContext restored =
                RequestContextHolder.require();

        assertEquals(
                activeTenant.toString(),
                restored.tenantId()
        );
    }

    @Test
    void targetSpaceMustBelongToSuppliedTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> gateway.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "Automated message",
                        "GT-SCHOOL",
                        "event-003",
                        "corr-003"
                )
        );

        verify(
                spaces
        ).findByIdAndTenantId(
                spaceId,
                tenantId
        );

        verifyNoInteractions(
                messages
        );

        assertTrue(
                RequestContextHolder.current()
                        .isEmpty()
        );
    }

    @Test
    void persistenceFailureStillRestoresPreviousContext() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContext previous =
                new RequestContext(
                        "original-after-failure",
                        tenantId.toString()
                );

        RequestContextHolder.set(
                previous
        );

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                spaceId
        );

        when(
                spaces.findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        when(
                messages.saveAndFlush(
                        any(ConnectMessage.class)
                )
        ).thenThrow(
                new IllegalStateException(
                        "simulated persistence failure"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> gateway.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "Automated message",
                        "EWE",
                        "workflow-failure",
                        "temporary-correlation"
                )
        );

        RequestContext restored =
                RequestContextHolder.require();

        assertEquals(
                "original-after-failure",
                restored.correlationId()
        );

        assertEquals(
                tenantId.toString(),
                restored.tenantId()
        );
    }
}
