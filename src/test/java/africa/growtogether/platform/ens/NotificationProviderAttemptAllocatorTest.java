package africa.growtogether.platform.ens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class NotificationProviderAttemptAllocatorTest {

    private NotificationRequestRepository notifications;
    private NotificationProviderAttemptRepository attempts;
    private NotificationProviderAttemptAllocator allocator;

    private UUID tenantId;
    private UUID notificationId;
    private UUID connectorId;
    private UUID routeId;

    @BeforeEach
    void setUp() {
        notifications =
                mock(NotificationRequestRepository.class);

        attempts =
                mock(NotificationProviderAttemptRepository.class);

        allocator =
                new NotificationProviderAttemptAllocator(
                        notifications,
                        attempts
                );

        tenantId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        connectorId = UUID.randomUUID();
        routeId = UUID.randomUUID();
    }

    @Test
    void allocatesFirstProviderAttemptAsOne() {
        lockNotification();

        when(
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationId
                        )
        ).thenReturn(Optional.empty());

        when(
                attempts.saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationProviderAttempt allocated =
                allocator.allocate(
                        tenantId,
                        notificationId,
                        connectorId,
                        routeId
                );

        assertThat(allocated.attemptNumber())
                .isEqualTo(1);

        assertThat(allocated.getTenantId())
                .isEqualTo(tenantId);

        assertThat(allocated.notificationRequestId())
                .isEqualTo(notificationId);

        assertThat(allocated.connectorId())
                .isEqualTo(connectorId);

        assertThat(allocated.routeId())
                .isEqualTo(routeId);

        assertThat(allocated.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.CREATED
                );
    }

    @Test
    void allocatesNextProviderAttemptAfterLatest() {
        lockNotification();

        NotificationProviderAttempt latest =
                mock(NotificationProviderAttempt.class);

        when(latest.attemptNumber())
                .thenReturn(7);

        when(
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationId
                        )
        ).thenReturn(Optional.of(latest));

        when(
                attempts.saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationProviderAttempt allocated =
                allocator.allocate(
                        tenantId,
                        notificationId,
                        connectorId,
                        routeId
                );

        assertThat(allocated.attemptNumber())
                .isEqualTo(8);
    }

    @Test
    void locksNotificationBeforeReadingLatestAttempt() {
        lockNotification();

        when(
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationId
                        )
        ).thenReturn(Optional.empty());

        when(
                attempts.saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        allocator.allocate(
                tenantId,
                notificationId,
                connectorId,
                routeId
        );

        InOrder order =
                inOrder(
                        notifications,
                        attempts
                );

        order.verify(notifications)
                .findByIdAndTenantIdForUpdate(
                        notificationId,
                        tenantId
                );

        order.verify(attempts)
                .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                        tenantId,
                        notificationId
                );

        order.verify(attempts)
                .saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                );
    }

    @Test
    void notificationLockIsTenantScoped() {
        lockNotification();

        when(
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationId
                        )
        ).thenReturn(Optional.empty());

        when(
                attempts.saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        allocator.allocate(
                tenantId,
                notificationId,
                connectorId,
                routeId
        );

        verify(notifications)
                .findByIdAndTenantIdForUpdate(
                        notificationId,
                        tenantId
                );

        verify(attempts)
                .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                        tenantId,
                        notificationId
                );
    }

    @Test
    void missingNotificationStopsAllocation() {
        when(
                notifications.findByIdAndTenantIdForUpdate(
                        notificationId,
                        tenantId
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> allocator.allocate(
                        tenantId,
                        notificationId,
                        connectorId,
                        routeId
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Notification request was not found"
                );

        verify(attempts, never())
                .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                        tenantId,
                        notificationId
                );

        verify(attempts, never())
                .saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                );
    }

    @Test
    void rejectsAttemptNumberOverflow() {
        lockNotification();

        NotificationProviderAttempt latest =
                mock(NotificationProviderAttempt.class);

        when(latest.attemptNumber())
                .thenReturn(Integer.MAX_VALUE);

        when(
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationId
                        )
        ).thenReturn(Optional.of(latest));

        assertThatThrownBy(
                () -> allocator.allocate(
                        tenantId,
                        notificationId,
                        connectorId,
                        routeId
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Provider attempt number limit reached"
                );

        verify(attempts, never())
                .saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                );
    }

    @Test
    void savedAttemptCarriesExactConnectorAndRoute() {
        lockNotification();

        when(
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationId
                        )
        ).thenReturn(Optional.empty());

        ArgumentCaptor<NotificationProviderAttempt> captor =
                ArgumentCaptor.forClass(
                        NotificationProviderAttempt.class
                );

        when(
                attempts.saveAndFlush(
                        org.mockito.ArgumentMatchers.any(
                                NotificationProviderAttempt.class
                        )
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        allocator.allocate(
                tenantId,
                notificationId,
                connectorId,
                routeId
        );

        verify(attempts)
                .saveAndFlush(
                        captor.capture()
                );

        assertThat(captor.getValue().connectorId())
                .isEqualTo(connectorId);

        assertThat(captor.getValue().routeId())
                .isEqualTo(routeId);
    }

    private void lockNotification() {
        NotificationRequest notification =
                mock(NotificationRequest.class);

        when(
                notifications.findByIdAndTenantIdForUpdate(
                        notificationId,
                        tenantId
                )
        ).thenReturn(Optional.of(notification));
    }
}
