package africa.growtogether.platform.school.academic.calendar.events;

import africa.growtogether.platform.communication.EnterpriseCommunicationCoordinator;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.DeliveryCommand;

import africa.growtogether.platform.connect.ConnectSpace;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationPriority;

import africa.growtogether.platform.school.integration.SchoolConnectSpaceResolver;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicCalendarEventNotificationListenerTest {

    @Mock
    private SchoolConnectSpaceResolver connectSpaces;

    @Mock
    private EnterpriseCommunicationCoordinator communications;

    private AcademicCalendarEventNotificationListener listener;

    @BeforeEach
    void setUp() {

        listener =
                new AcademicCalendarEventNotificationListener(
                        connectSpaces,
                        communications
                );
    }

    @Test
    void notificationDisabledProducesNoCommunication() {

        AcademicCalendarEventCreatedEvent event =
                event(
                        false
                );

        listener.handle(
                event
        );

        verifyNoInteractions(
                connectSpaces,
                communications
        );
    }

    @Test
    void notificationEnabledDeliversConnectAndEnsTogether() {

        UUID tenantId =
                UUID.randomUUID();

        UUID eventId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        ConnectSpace institutionSpace =
                mock(
                        ConnectSpace.class
                );

        when(
                institutionSpace.getId()
        ).thenReturn(
                spaceId
        );

        when(
                connectSpaces.requireInstitutionSpace(
                        tenantId
                )
        ).thenReturn(
                institutionSpace
        );

        AcademicCalendarEventCreatedEvent event =
                new AcademicCalendarEventCreatedEvent(
                        eventId,
                        tenantId,
                        "TERM_EVENT",
                        "Parents Meeting",
                        Instant.parse(
                                "2026-09-15T08:00:00Z"
                        ),
                        true,
                        Instant.parse(
                                "2026-08-26T11:00:00Z"
                        )
                );

        listener.handle(
                event
        );

        verify(
                connectSpaces
        ).requireInstitutionSpace(
                tenantId
        );

        ArgumentCaptor<DeliveryCommand> captor =
                ArgumentCaptor.forClass(
                        DeliveryCommand.class
                );

        verify(
                communications
        ).deliver(
                captor.capture()
        );

        DeliveryCommand command =
                captor.getValue();

        assertEquals(
                tenantId,
                command.tenantId()
        );

        assertEquals(
                "GT-SCHOOL",
                command.sourceService()
        );

        assertEquals(
                eventId.toString(),
                command.sourceReference()
        );

        /*
         * The coordinator should reuse the active request
         * correlation ID or generate one when absent.
         */
        assertNull(
                command.correlationId()
        );

        assertNotNull(
                command.connect()
        );

        assertEquals(
                spaceId,
                command.connect().spaceId()
        );

        assertEquals(
                "Academic calendar event created: Parents Meeting",
                command.connect().body()
        );

        assertNotNull(
                command.notification()
        );

        assertEquals(
                "GT-SCHOOL-CALENDAR-EVENT",
                command.notification().definitionCode()
        );

        assertEquals(
                "SCHOOL_USERS",
                command.notification().recipient()
        );

        assertEquals(
                NotificationChannel.IN_APP,
                command.notification().channel()
        );

        assertEquals(
                NotificationPriority.NORMAL,
                command.notification().priority()
        );

        assertEquals(
                "Parents Meeting",
                command.notification().subject()
        );

        assertEquals(
                "Academic calendar event created: Parents Meeting",
                command.notification().body()
        );
    }

    @Test
    void missingCanonicalSchoolSpacePreventsCommunication() {

        UUID tenantId =
                UUID.randomUUID();

        AcademicCalendarEventCreatedEvent event =
                new AcademicCalendarEventCreatedEvent(
                        UUID.randomUUID(),
                        tenantId,
                        "TERM_EVENT",
                        "Sports Day",
                        Instant.now(),
                        true,
                        Instant.now()
                );

        when(
                connectSpaces.requireInstitutionSpace(
                        tenantId
                )
        ).thenThrow(
                new IllegalStateException(
                        "Canonical GT Connect institution space "
                                + "is not provisioned for the school"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> listener.handle(
                        event
                )
        );

        verifyNoInteractions(
                communications
        );
    }

    private AcademicCalendarEventCreatedEvent event(
            boolean notificationRequired
    ) {

        return new AcademicCalendarEventCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TERM_EVENT",
                "School Event",
                Instant.now(),
                notificationRequired,
                Instant.now()
        );
    }
}
