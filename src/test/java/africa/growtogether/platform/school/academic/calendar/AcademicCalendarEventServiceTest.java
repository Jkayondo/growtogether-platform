package africa.growtogether.platform.school.academic.calendar;

import africa.growtogether.platform.common.events.EventPublisher;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AcademicCalendarEventServiceTest {
    private final AcademicCalendarEventRepository repository =
            mock(AcademicCalendarEventRepository.class);
    private final EventPublisher publisher = mock(EventPublisher.class);
    private final EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
    private final UUID tenant = UUID.randomUUID();
    private final Instant start = Instant.parse("2026-09-10T06:00:00Z");
    private final Instant end = start.plusSeconds(3600);
    private AcademicCalendarEventService service;

    @BeforeEach
    void setup() {
        service = new AcademicCalendarEventService(repository, publisher, identity);
        when(identity.requireTenantId()).thenReturn(tenant);
    }

    @Test
    void upcomingUsesAuthenticatedTenant() {
        var event = mock(AcademicCalendarEvent.class);
        when(repository.findByTenantIdAndStartAtBetween(tenant, start, end))
                .thenReturn(List.of(event));
        assertEquals(List.of(event), service.findUpcomingEvents(start, end));
        verify(repository).findByTenantIdAndStartAtBetween(tenant, start, end);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void otherCalendarReadsAreTenantScoped() {
        UUID year = UUID.randomUUID();
        UUID term = UUID.randomUUID();
        service.findByAcademicYear(year);
        service.findByAcademicTerm(term);
        service.findScheduledEvents();
        verify(repository).findByTenantIdAndAcademicYearId(tenant, year);
        verify(repository).findByTenantIdAndAcademicTermId(tenant, term);
        verify(repository).findByTenantIdAndEventStatus(tenant, "SCHEDULED");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void missingIdentityCannotQueryEvents() {
        when(identity.requireTenantId()).thenThrow(new AccessDeniedException("No identity"));
        assertThrows(AccessDeniedException.class, () -> service.findUpcomingEvents(start, end));
        verifyNoInteractions(repository);
    }

    @Test
    void invalidQueryIntervalCannotReachRepository() {
        assertThrows(IllegalArgumentException.class, () -> service.findUpcomingEvents(end, start));
        assertThrows(IllegalArgumentException.class, () -> service.findUpcomingEvents(null, end));
        verifyNoInteractions(repository);
    }

    @Test
    void creationSavesEndTime() {
        AcademicYear year = year(tenant);
        when(repository.save(any(AcademicCalendarEvent.class)))
                .thenAnswer(call -> call.getArgument(0));
        AcademicCalendarEvent saved = create(year, null, end);
        assertEquals(end, saved.getEndAt());
        assertEquals(start, saved.getStartAt());
        assertEquals(tenant, saved.getTenantId());
        verify(identity).requireTenant(tenant);
        verify(repository).save(saved);
    }

    @Test
    void creationAllowsAbsentEndTime() {
        when(repository.save(any(AcademicCalendarEvent.class)))
                .thenAnswer(call -> call.getArgument(0));
        assertNull(create(year(tenant), null, null).getEndAt());
    }

    @Test
    void crossTenantYearCannotBeSaved() {
        assertThrows(AccessDeniedException.class,
                () -> create(year(UUID.randomUUID()), null, end));
        verifyNoInteractions(repository, publisher);
    }

    @Test
    void crossTenantRequestCannotBeSaved() {
        doThrow(new AccessDeniedException("Wrong tenant"))
                .when(identity).requireTenant(tenant);
        assertThrows(AccessDeniedException.class,
                () -> create(year(tenant), null, end));
        verifyNoInteractions(repository, publisher);
    }

    @Test
    void crossTenantTermCannotBeSaved() {
        AcademicTerm term = mock(AcademicTerm.class);
        when(term.getTenantId()).thenReturn(UUID.randomUUID());
        assertThrows(AccessDeniedException.class,
                () -> create(year(tenant), term, end));
        verifyNoInteractions(repository, publisher);
    }

    @Test
    void mismatchedTermYearCannotBeSaved() {
        AcademicTerm term = mock(AcademicTerm.class);
        when(term.getTenantId()).thenReturn(tenant);
        AcademicYear otherYear = year(tenant);
        when(term.getAcademicYear()).thenReturn(otherYear);
        assertThrows(IllegalArgumentException.class,
                () -> create(year(tenant), term, end));
        verifyNoInteractions(repository, publisher);
    }

    @Test
    void endBeforeStartCannotBeSaved() {
        assertThrows(IllegalArgumentException.class,
                () -> create(year(tenant), null, start.minusSeconds(1)));
        verifyNoInteractions(repository, publisher);
    }

    private AcademicYear year(UUID tenantId) {
        AcademicYear year = mock(AcademicYear.class);
        when(year.getTenantId()).thenReturn(tenantId);
        when(year.getId()).thenReturn(UUID.randomUUID());
        return year;
    }

    private AcademicCalendarEvent create(AcademicYear year, AcademicTerm term, Instant endAt) {
        return service.create(tenant, year, term, "TEST", "Test event",
                "TERM_EVENT", start, endAt);
    }
}
