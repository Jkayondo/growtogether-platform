package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.connect.ConnectTeacherAuthorizationService;
import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherProgrammeDayServiceTest {
    private final EnterpriseIdentityContext identity =
            mock(EnterpriseIdentityContext.class);
    private final ConnectTeacherAuthorizationService teachers =
            mock(ConnectTeacherAuthorizationService.class);
    private final SchoolProfileService schools =
            mock(SchoolProfileService.class);
    private final UUID tenant = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();

    private TeacherProgrammeDayService service(String now) {
        return new TeacherProgrammeDayService(
                identity, teachers, schools,
                Clock.fixed(Instant.parse(now), ZoneOffset.UTC));
    }

    private void authorized(String zone) {
        TeacherProfile teacher = mock(TeacherProfile.class);
        when(teacher.getId()).thenReturn(teacherId);
        when(identity.requireTenantId()).thenReturn(tenant);
        when(teachers.requireUniqueCurrentTeacherProfile()).thenReturn(teacher);
        when(schools.requireTimezone(tenant)).thenReturn(ZoneId.of(zone));
    }

    @Test
    void usesSchoolDateWhenUtcIsStillPreviousDay() {
        authorized("Africa/Kampala");
        var day = service("2026-09-13T22:30:00Z").currentDay();

        assertThat(day.tenantId()).isEqualTo(tenant);
        assertThat(day.teacherProfileId()).isEqualTo(teacherId);
        assertThat(day.date()).isEqualTo(LocalDate.of(2026, 9, 14));
        assertThat(day.zone()).isEqualTo(ZoneId.of("Africa/Kampala"));
        assertThat(day.startInclusive())
                .isEqualTo(Instant.parse("2026-09-13T21:00:00Z"));
        assertThat(day.endExclusive())
                .isEqualTo(Instant.parse("2026-09-14T21:00:00Z"));
    }

    @Test
    void springClockChangeProducesTwentyThreeHourDay() {
        authorized("Europe/London");
        var day = service("2026-03-29T12:00:00Z").currentDay();
        assertThat(Duration.between(day.startInclusive(), day.endExclusive()))
                .isEqualTo(Duration.ofHours(23));
    }

    @Test
    void autumnClockChangeProducesTwentyFiveHourDay() {
        authorized("Europe/London");
        var day = service("2026-10-25T12:00:00Z").currentDay();
        assertThat(Duration.between(day.startInclusive(), day.endExclusive()))
                .isEqualTo(Duration.ofHours(25));
    }

    @Test
    void missingIdentityStopsBeforeTeacherAndSchoolLookups() {
        when(identity.requireTenantId())
                .thenThrow(new AccessDeniedException("Missing identity"));

        assertThatThrownBy(() ->
                service("2026-09-14T12:00:00Z").currentDay())
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(teachers, schools);
    }

    @Test
    void rejectedTeacherStopsBeforeSchoolLookup() {
        when(identity.requireTenantId()).thenReturn(tenant);
        when(teachers.requireUniqueCurrentTeacherProfile())
                .thenThrow(new AccessDeniedException("Teacher unavailable"));

        assertThatThrownBy(() ->
                service("2026-09-14T12:00:00Z").currentDay())
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(schools);
    }

    @Test
    void missingTimezoneDoesNotSilentlyFallBack() {
        authorized("Africa/Kampala");
        when(schools.requireTimezone(tenant))
                .thenThrow(new IllegalStateException("School timezone is not configured"));

        assertThatThrownBy(() ->
                service("2026-09-14T12:00:00Z").currentDay())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("School timezone is not configured");
    }
}
