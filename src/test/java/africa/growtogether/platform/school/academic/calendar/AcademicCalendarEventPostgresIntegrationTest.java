package africa.growtogether.platform.school.academic.calendar;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.school.academic.year.*;
import africa.growtogether.platform.school.academic.term.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AcademicCalendarEventPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }

    @Autowired AcademicCalendarEventService service;
    @Autowired AcademicCalendarEventRepository events;
    @Autowired AcademicYearRepository years;
    @Autowired AcademicTermRepository terms;
    @Autowired PlatformTransactionManager manager;
    @Autowired JdbcTemplate jdbc;

    private TransactionTemplate transaction;
    private Fixture first;
    private Fixture second;
    private static final Instant START = Instant.parse("2035-09-10T06:00:00Z");
    private static final Instant END = START.plusSeconds(3600);

    private record Fixture(UUID tenant, UUID year, UUID term, UUID event) {}

    @BeforeEach
    void setup() {
        transaction = new TransactionTemplate(manager);
        UUID firstTenant = jdbc.queryForObject(
                "SELECT id FROM eiam_tenant WHERE code = 'GT-SCHOOL'", UUID.class);
        UUID organization = jdbc.queryForObject(
                "SELECT organization_id FROM eiam_tenant WHERE id = ?",
                UUID.class, firstTenant);
        UUID secondTenant = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO eiam_tenant "
                + "(id, organization_id, code, name, status, created_at, version) "
                + "VALUES (?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, 0)",
                secondTenant, organization,
                "CALENDAR-TEST-" + secondTenant,
                "Calendar isolation test " + secondTenant);
        first = seed(firstTenant, END);
        second = seed(secondTenant, null);
        authenticate(first.tenant());
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.clear();
    }

    private void authenticate(UUID tenant) {
        RequestContextHolder.set(new RequestContext("calendar-postgres-test", tenant.toString()));
        GtPrincipal principal = new GtPrincipal(
                UUID.randomUUID(), "calendar-postgres-test", tenant,
                Set.of("SCHOOL_ADMIN"),
                Set.of("school.academic.calendar.read", "school.academic.calendar.create"),
                UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Fixture seed(UUID tenant, Instant endAt) {
        authenticate(tenant);
        return transaction.execute(status -> {
            String suffix = UUID.randomUUID().toString();
            AcademicYear year = years.saveAndFlush(new AcademicYear(
                    tenant, suffix, "Calendar test " + suffix,
                    LocalDate.of(2035, 1, 1), LocalDate.of(2035, 12, 31)));
            AcademicTerm term = new AcademicTerm(
                    year, "T1", "Test term",
                    LocalDate.of(2035, 9, 1), LocalDate.of(2035, 11, 30), 1);
            term.setTenantId(tenant);
            term = terms.saveAndFlush(term);
            AcademicCalendarEvent event = service.create(
                    tenant, year, term, "EVENT-" + suffix, "Test meeting",
                    "STAFF_MEETING", START, endAt);
            events.flush();
            return new Fixture(tenant, year.getId(), term.getId(), event.getId());
        });
    }

    @Test
    void upcomingAndScheduledQueriesExcludeOtherTenantRecords() {
        transaction.executeWithoutResult(status -> {
            List<AcademicCalendarEvent> upcoming = service.findUpcomingEvents(
                    START.minusSeconds(1), END.plusSeconds(1));
            assertThat(upcoming).isNotEmpty();
            assertThat(upcoming).allMatch(e -> first.tenant().equals(e.getTenantId()));
            assertThat(upcoming).extracting(AcademicCalendarEvent::getId)
                    .contains(first.event()).doesNotContain(second.event());

            List<AcademicCalendarEvent> scheduled = service.findScheduledEvents();
            assertThat(scheduled).isNotEmpty();
            assertThat(scheduled).allMatch(e -> first.tenant().equals(e.getTenantId()));
            assertThat(scheduled).extracting(AcademicCalendarEvent::getId)
                    .contains(first.event()).doesNotContain(second.event());
        });

        authenticate(second.tenant());
        transaction.executeWithoutResult(status ->
            assertThat(service.findUpcomingEvents(START.minusSeconds(1), END.plusSeconds(1)))
                .isNotEmpty()
                .allMatch(e -> second.tenant().equals(e.getTenantId())));
    }

    @Test
    void yearAndTermQueriesRejectForeignScopeByReturningNoRecords() {
        transaction.executeWithoutResult(status -> {
            assertThat(service.findByAcademicYear(first.year()))
                    .extracting(AcademicCalendarEvent::getId).containsExactly(first.event());
            assertThat(service.findByAcademicTerm(first.term()))
                    .extracting(AcademicCalendarEvent::getId).containsExactly(first.event());
            assertThat(service.findByAcademicYear(second.year())).isEmpty();
            assertThat(service.findByAcademicTerm(second.term())).isEmpty();
        });
    }

    @Test
    void endTimeSurvivesCommitAndReloadIncludingNull() {
        transaction.executeWithoutResult(status -> {
            AcademicCalendarEvent saved = events.findById(first.event()).orElseThrow();
            assertThat(saved.getStartAt()).isEqualTo(START);
            assertThat(saved.getEndAt()).isEqualTo(END);
        });

        authenticate(second.tenant());
        transaction.executeWithoutResult(status -> {
            AcademicCalendarEvent saved = events.findById(second.event()).orElseThrow();
            assertThat(saved.getStartAt()).isEqualTo(START);
            assertThat(saved.getEndAt()).isNull();
        });
    }
}
