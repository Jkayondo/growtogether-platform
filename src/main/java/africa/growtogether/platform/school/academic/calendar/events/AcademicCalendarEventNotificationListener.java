package africa.growtogether.platform.school.academic.calendar.events;


import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationPriority;
import africa.growtogether.platform.ens.NotificationService;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class AcademicCalendarEventNotificationListener {


    private final NotificationService notificationService;


    public AcademicCalendarEventNotificationListener(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }


    @EventListener
    public void handle(
            AcademicCalendarEventCreatedEvent event
    ) {


        if (!event.notificationRequired()) {
            return;
        }


        notificationService.sendForTenant(
                event.tenantId(),

                new SendCommand(
                        "GT-SCHOOL-CALENDAR-EVENT",
                        "SCHOOL_USERS",
                        NotificationChannel.IN_APP,
                        NotificationPriority.NORMAL,
                        event.eventName(),
                        "Academic calendar event created: "
                                + event.eventName(),
                        "GT-SCHOOL",
                        event.eventId().toString()
                )
        );
    }
}
