package africa.growtogether.platform.school.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgrammeCalendarEventProjectorTest {

    @Test
    void projectsDisplayReadyCalendarEvent() {

        AcademicCalendarEvent event =
                mock(AcademicCalendarEvent.class);

        UUID id = UUID.randomUUID();

        Instant start =
                Instant.parse(
                        "2026-09-24T08:00:00Z"
                );

        Instant end =
                Instant.parse(
                        "2026-09-24T09:00:00Z"
                );

        when(event.getId())
                .thenReturn(id);

        when(event.getEventCode())
                .thenReturn("ASSEMBLY");

        when(event.getEventName())
                .thenReturn("Morning Assembly");

        when(event.getEventType())
                .thenReturn("ASSEMBLY");

        when(event.getStartAt())
                .thenReturn(start);

        when(event.getEndAt())
                .thenReturn(end);

        when(event.getEventStatus())
                .thenReturn("CONFIRMED");

        ProgrammeCalendarEventProjector projector =
                new ProgrammeCalendarEventProjector();

        ProgrammeCalendarEvent detail =
                projector
                        .project(
                                List.of(event)
                        )
                        .get(0);

        assertThat(detail.id())
                .isEqualTo(id);

        assertThat(detail.eventCode())
                .isEqualTo("ASSEMBLY");

        assertThat(detail.eventName())
                .isEqualTo("Morning Assembly");

        assertThat(detail.startAt())
                .isEqualTo(start);

        assertThat(detail.endAt())
                .isEqualTo(end);

        assertThat(detail.eventStatus())
                .isEqualTo("CONFIRMED");
    }

    @Test
    void failsClosedOnNullCalendarEvent() {

        ProgrammeCalendarEventProjector projector =
                new ProgrammeCalendarEventProjector();

        assertThatThrownBy(
                () ->
                        projector.project(
                                java.util.Arrays.asList(
                                        (AcademicCalendarEvent) null
                                )
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Programme calendar query returned null event"
                );
    }
}
