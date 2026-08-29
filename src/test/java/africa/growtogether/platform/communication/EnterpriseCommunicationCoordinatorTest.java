package africa.growtogether.platform.communication;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.ConnectIntent;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.DeliveryCommand;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.DeliveryResult;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.NotificationIntent;

import africa.growtogether.platform.connect.ConnectMessage;
import africa.growtogether.platform.connect.integration.ConnectInternalMessageGateway;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos;
import africa.growtogether.platform.ens.NotificationPriority;
import africa.growtogether.platform.ens.NotificationService;
import africa.growtogether.platform.ens.NotificationStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseCommunicationCoordinatorTest {

    @Mock
    private ConnectInternalMessageGateway connect;

    @Mock
    private NotificationService notifications;

    private EnterpriseCommunicationCoordinator coordinator;

    @BeforeEach
    void setUp() {

        RequestContextHolder.clear();

        coordinator =
                new EnterpriseCommunicationCoordinator(
                        connect,
                        notifications
                );
    }

    @AfterEach
    void clearContext() {

        RequestContextHolder.clear();
    }

    @Test
    void connectOnlyDeliveryUsesInternalConnectGateway() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        ConnectMessage connectMessage =
                mock(
                        ConnectMessage.class
                );

        when(
                connect.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "Internal school communication.",
                        "GT-SCHOOL",
                        "event-001",
                        "corr-connect"
                )
        ).thenReturn(
                connectMessage
        );

        DeliveryResult result =
                coordinator.deliver(
                        new DeliveryCommand(
                                tenantId,
                                "GT-SCHOOL",
                                "event-001",
                                "corr-connect",
                                new ConnectIntent(
                                        spaceId,
                                        "Internal school communication."
                                ),
                                null
                        )
                );

        assertSame(
                connectMessage,
                result.connectMessage()
        );

        assertNull(
                result.notification()
        );

        assertEquals(
                "corr-connect",
                result.correlationId()
        );

        verify(
                connect
        ).sendSystemMessage(
                tenantId,
                spaceId,
                "Internal school communication.",
                "GT-SCHOOL",
                "event-001",
                "corr-connect"
        );

        verifyNoInteractions(
                notifications
        );
    }

    @Test
    void ensOnlyDeliveryDoesNotCreateConnectMessage() {

        UUID tenantId =
                UUID.randomUUID();

        NotificationDtos.View notification =
                notification(
                        tenantId,
                        NotificationChannel.EMAIL
                );

        when(
                notifications.sendForTenant(
                        eq(tenantId),
                        any(NotificationDtos.SendCommand.class)
                )
        ).thenReturn(
                notification
        );

        DeliveryResult result =
                coordinator.deliver(
                        new DeliveryCommand(
                                tenantId,
                                "GT-SCHOOL",
                                "notice-001",
                                "corr-ens",
                                null,
                                new NotificationIntent(
                                        "GT-SCHOOL-NOTICE",
                                        "parent@example.com",
                                        NotificationChannel.EMAIL,
                                        NotificationPriority.NORMAL,
                                        "School notice",
                                        "A school notice is available."
                                )
                        )
                );

        assertNull(
                result.connectMessage()
        );

        assertSame(
                notification,
                result.notification()
        );

        assertEquals(
                "corr-ens",
                result.correlationId()
        );

        verifyNoInteractions(
                connect
        );

        ArgumentCaptor<NotificationDtos.SendCommand> command =
                ArgumentCaptor.forClass(
                        NotificationDtos.SendCommand.class
                );

        verify(
                notifications
        ).sendForTenant(
                eq(tenantId),
                command.capture()
        );

        assertEquals(
                "GT-SCHOOL-NOTICE",
                command.getValue()
                        .definitionCode()
        );

        assertEquals(
                "GT-SCHOOL",
                command.getValue()
                        .sourceService()
        );

        assertEquals(
                "notice-001",
                command.getValue()
                        .sourceReference()
        );
    }

    @Test
    void dualDeliveryUsesSameSourceAndCorrelationContext() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        ConnectMessage connectMessage =
                mock(
                        ConnectMessage.class
                );

        NotificationDtos.View notification =
                notification(
                        tenantId,
                        NotificationChannel.PUSH
                );

        when(
                connect.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "Calendar event tomorrow.",
                        "GT-SCHOOL",
                        "calendar-001",
                        "shared-correlation"
                )
        ).thenReturn(
                connectMessage
        );

        when(
                notifications.sendForTenant(
                        eq(tenantId),
                        any(NotificationDtos.SendCommand.class)
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
                            "shared-correlation",
                            active.correlationId()
                    );

                    return notification;
                }
        );

        DeliveryResult result =
                coordinator.deliver(
                        new DeliveryCommand(
                                tenantId,
                                "GT-SCHOOL",
                                "calendar-001",
                                "shared-correlation",
                                new ConnectIntent(
                                        spaceId,
                                        "Calendar event tomorrow."
                                ),
                                new NotificationIntent(
                                        "GT-SCHOOL-CALENDAR-EVENT",
                                        "SCHOOL_USERS",
                                        NotificationChannel.PUSH,
                                        NotificationPriority.NORMAL,
                                        "Calendar Event",
                                        "Calendar event tomorrow."
                                )
                        )
                );

        assertSame(
                connectMessage,
                result.connectMessage()
        );

        assertSame(
                notification,
                result.notification()
        );

        assertEquals(
                "shared-correlation",
                result.correlationId()
        );

        verify(
                connect
        ).sendSystemMessage(
                tenantId,
                spaceId,
                "Calendar event tomorrow.",
                "GT-SCHOOL",
                "calendar-001",
                "shared-correlation"
        );

        verify(
                notifications
        ).sendForTenant(
                eq(tenantId),
                any(NotificationDtos.SendCommand.class)
        );
    }

    @Test
    void rejectsCommandWithoutAnyDeliveryIntent() {

        UUID tenantId =
                UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> coordinator.deliver(
                        new DeliveryCommand(
                                tenantId,
                                "GT-SCHOOL",
                                "event-empty",
                                "corr-empty",
                                null,
                                null
                        )
                )
        );

        verifyNoInteractions(
                connect,
                notifications
        );
    }

    @Test
    void crossTenantActiveContextIsRejectedBeforeDelivery() {

        UUID activeTenant =
                UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "existing-context",
                        activeTenant.toString()
                )
        );

        assertThrows(
                TenantScopeViolationException.class,
                () -> coordinator.deliver(
                        new DeliveryCommand(
                                hostileTenant,
                                "GT-SCHOOL",
                                "event-hostile",
                                "hostile-correlation",
                                new ConnectIntent(
                                        spaceId,
                                        "Cross-tenant communication."
                                ),
                                null
                        )
                )
        );

        verifyNoInteractions(
                connect,
                notifications
        );

        RequestContext restored =
                RequestContextHolder.require();

        assertEquals(
                activeTenant.toString(),
                restored.tenantId()
        );

        assertEquals(
                "existing-context",
                restored.correlationId()
        );
    }

    @Test
    void previousCompatibleContextIsRestoredAfterSuccessfulDelivery() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "original-correlation",
                        tenantId.toString()
                )
        );

        when(
                connect.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "Internal communication.",
                        "EWE",
                        "workflow-001",
                        "temporary-correlation"
                )
        ).thenReturn(
                mock(ConnectMessage.class)
        );

        coordinator.deliver(
                new DeliveryCommand(
                        tenantId,
                        "EWE",
                        "workflow-001",
                        "temporary-correlation",
                        new ConnectIntent(
                                spaceId,
                                "Internal communication."
                        ),
                        null
                )
        );

        RequestContext restored =
                RequestContextHolder.require();

        assertEquals(
                tenantId.toString(),
                restored.tenantId()
        );

        assertEquals(
                "original-correlation",
                restored.correlationId()
        );
    }

    @Test
    void downstreamFailureStillRestoresPreviousContext() {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "before-failure",
                        tenantId.toString()
                )
        );

        when(
                connect.sendSystemMessage(
                        tenantId,
                        spaceId,
                        "Message that fails.",
                        "GT-SCHOOL",
                        "event-failure",
                        "during-failure"
                )
        ).thenThrow(
                new IllegalStateException(
                        "simulated Connect persistence failure"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> coordinator.deliver(
                        new DeliveryCommand(
                                tenantId,
                                "GT-SCHOOL",
                                "event-failure",
                                "during-failure",
                                new ConnectIntent(
                                        spaceId,
                                        "Message that fails."
                                ),
                                new NotificationIntent(
                                        "GT-SCHOOL-TEST",
                                        "SCHOOL_USERS",
                                        NotificationChannel.IN_APP,
                                        NotificationPriority.NORMAL,
                                        "Test",
                                        "Test notification"
                                )
                        )
                )
        );

        /*
         * Connect executes first. If it fails, ENS must not create
         * a notification that would imply successful coordinated delivery.
         */
        verifyNoInteractions(
                notifications
        );

        RequestContext restored =
                RequestContextHolder.require();

        assertEquals(
                tenantId.toString(),
                restored.tenantId()
        );

        assertEquals(
                "before-failure",
                restored.correlationId()
        );
    }

    private NotificationDtos.View notification(
            UUID tenantId,
            NotificationChannel channel
    ) {

        return new NotificationDtos.View(
                UUID.randomUUID(),
                tenantId,
                "GT-TEST",
                "recipient",
                channel,
                NotificationPriority.NORMAL,
                NotificationStatus.QUEUED,
                "Test subject",
                "GT-SCHOOL",
                "test-reference",
                0,
                null,
                null,
                null
        );
    }
}
