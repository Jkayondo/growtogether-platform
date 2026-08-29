package africa.growtogether.platform.school.academic.calendar.events;

import africa.growtogether.platform.communication.EnterpriseCommunicationCoordinator;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.ConnectIntent;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.DeliveryCommand;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.NotificationIntent;

import africa.growtogether.platform.connect.ConnectSpace;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationPriority;

import africa.growtogether.platform.school.integration.SchoolConnectSpaceResolver;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AcademicCalendarEventNotificationListener {

    private final SchoolConnectSpaceResolver
            connectSpaces;

    private final EnterpriseCommunicationCoordinator
            communications;

    public AcademicCalendarEventNotificationListener(
            SchoolConnectSpaceResolver connectSpaces,
            EnterpriseCommunicationCoordinator communications
    ) {
        this.connectSpaces =
                connectSpaces;

        this.communications =
                communications;
    }

    @EventListener
    public void handle(
            AcademicCalendarEventCreatedEvent event
    ) {

        /*
         * Preserve the existing GT School rule:
         * calendar events generate communication only when
         * notificationRequired is explicitly enabled.
         */
        if (!event.notificationRequired()) {
            return;
        }

        ConnectSpace institutionSpace =
                connectSpaces.requireInstitutionSpace(
                        event.tenantId()
                );

        String body =
                "Academic calendar event created: "
                        + event.eventName();

        communications.deliver(
                new DeliveryCommand(
                        event.tenantId(),
                        "GT-SCHOOL",
                        event.eventId().toString(),

                        /*
                         * Null deliberately allows the enterprise
                         * coordinator to preserve the current request
                         * correlation ID, or generate one if none exists.
                         */
                        null,

                        new ConnectIntent(
                                institutionSpace.getId(),
                                body
                        ),

                        new NotificationIntent(
                                "GT-SCHOOL-CALENDAR-EVENT",
                                "SCHOOL_USERS",
                                NotificationChannel.IN_APP,
                                NotificationPriority.NORMAL,
                                event.eventName(),
                                body
                        )
                )
        );
    }
}
