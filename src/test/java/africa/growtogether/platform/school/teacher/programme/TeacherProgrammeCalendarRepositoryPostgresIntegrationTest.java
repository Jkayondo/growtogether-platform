package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEventRepository;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEventService;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TeacherProgrammeCalendarRepositoryPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            );

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );
        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    TeacherProgrammeCalendarRepository programmeCalendar;

    @Autowired
    AcademicCalendarEventService calendarService;

    @Autowired
    AcademicCalendarEventRepository calendarEvents;

    @Autowired
    AcademicYearRepository years;

    @Autowired
    AcademicTermRepository terms;

    @Autowired
    PlatformTransactionManager manager;

    @Autowired
    JdbcTemplate jdbc;

    private TransactionTemplate transaction;

    private TenantFixture primary;
    private TenantFixture foreign;

    private static final Instant DAY_START =
            Instant.parse(
                    "2035-09-10T00:00:00Z"
            );

    private static final Instant DAY_END =
            Instant.parse(
                    "2035-09-11T00:00:00Z"
            );

    private record TenantFixture(
            UUID tenant,
            AcademicYear year,
            AcademicTerm term
    ) {
    }

    @BeforeEach
    void setUp() {

        transaction =
                new TransactionTemplate(
                        manager
                );

        UUID baselineTenant =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE code = 'GT-SCHOOL'
                        """,
                        UUID.class
                );

        UUID organization =
                jdbc.queryForObject(
                        """
                        SELECT organization_id
                        FROM eiam_tenant
                        WHERE id = ?
                        """,
                        UUID.class,
                        baselineTenant
                );

        UUID primaryTenant =
                createIsolatedTenant(
                        organization,
                        "PRIMARY"
                );

        UUID foreignTenant =
                createIsolatedTenant(
                        organization,
                        "FOREIGN"
                );

        primary =
                createTenantFixture(
                        primaryTenant
                );

        foreign =
                createTenantFixture(
                        foreignTenant
                );

        authenticate(
                primary.tenant()
        );
    }

    @AfterEach
    void clearContext() {

        SecurityContextHolder
                .clearContext();

        RequestContextHolder
                .clear();
    }

    @Test
    void appliesOverlapBoundaryNullEndZeroDurationAndOrdering() {

        AcademicCalendarEvent crossMidnight =
                createEvent(
                        primary,
                        "CROSS",
                        DAY_START.minusSeconds(3600),
                        DAY_START.plusSeconds(1800),
                        "SCHEDULED"
                );

        AcademicCalendarEvent exactStart =
                createEvent(
                        primary,
                        "START",
                        DAY_START,
                        DAY_START.plusSeconds(900),
                        "SCHEDULED"
                );

        AcademicCalendarEvent nullEndInside =
                createEvent(
                        primary,
                        "NULL-END",
                        DAY_START.plusSeconds(3600),
                        null,
                        "SCHEDULED"
                );

        AcademicCalendarEvent zeroDurationInside =
                createEvent(
                        primary,
                        "ZERO",
                        DAY_START.plusSeconds(7200),
                        DAY_START.plusSeconds(7200),
                        "SCHEDULED"
                );

        AcademicCalendarEvent ordinaryInside =
                createEvent(
                        primary,
                        "INSIDE",
                        DAY_START.plusSeconds(10800),
                        DAY_START.plusSeconds(11700),
                        "CONFIRMED"
                );

        AcademicCalendarEvent endsExactlyAtStart =
                createEvent(
                        primary,
                        "ENDS-AT-START",
                        DAY_START.minusSeconds(3600),
                        DAY_START,
                        "SCHEDULED"
                );

        AcademicCalendarEvent nullEndBeforeDay =
                createEvent(
                        primary,
                        "NULL-BEFORE",
                        DAY_START.minusSeconds(60),
                        null,
                        "SCHEDULED"
                );

        AcademicCalendarEvent zeroDurationBeforeDay =
                createEvent(
                        primary,
                        "ZERO-BEFORE",
                        DAY_START.minusSeconds(120),
                        DAY_START.minusSeconds(120),
                        "SCHEDULED"
                );

        AcademicCalendarEvent startsAtEndExclusive =
                createEvent(
                        primary,
                        "STARTS-AT-END",
                        DAY_END,
                        DAY_END.plusSeconds(3600),
                        "SCHEDULED"
                );

        List<AcademicCalendarEvent> result =
                queryPrimary();

        assertThat(result)
                .extracting(
                        AcademicCalendarEvent::getId
                )
                .containsExactly(
                        crossMidnight.getId(),
                        exactStart.getId(),
                        nullEndInside.getId(),
                        zeroDurationInside.getId(),
                        ordinaryInside.getId()
                );

        assertThat(result)
                .extracting(
                        AcademicCalendarEvent::getId
                )
                .doesNotContain(
                        endsExactlyAtStart.getId(),
                        nullEndBeforeDay.getId(),
                        zeroDurationBeforeDay.getId(),
                        startsAtEndExclusive.getId()
                );
    }

    @Test
    void enforcesTenantAndLifecycleVisibility() {

        AcademicCalendarEvent scheduled =
                createEvent(
                        primary,
                        "SCHEDULED",
                        DAY_START.plusSeconds(1000),
                        DAY_START.plusSeconds(1100),
                        "SCHEDULED"
                );

        AcademicCalendarEvent confirmed =
                createEvent(
                        primary,
                        "CONFIRMED",
                        DAY_START.plusSeconds(2000),
                        DAY_START.plusSeconds(2100),
                        "CONFIRMED"
                );

        AcademicCalendarEvent inProgress =
                createEvent(
                        primary,
                        "IN-PROGRESS",
                        DAY_START.plusSeconds(3000),
                        DAY_START.plusSeconds(3100),
                        "IN_PROGRESS"
                );

        AcademicCalendarEvent completed =
                createEvent(
                        primary,
                        "COMPLETED",
                        DAY_START.plusSeconds(4000),
                        DAY_START.plusSeconds(4100),
                        "COMPLETED"
                );

        AcademicCalendarEvent postponed =
                createEvent(
                        primary,
                        "POSTPONED",
                        DAY_START.plusSeconds(5000),
                        DAY_START.plusSeconds(5100),
                        "POSTPONED"
                );

        AcademicCalendarEvent cancelled =
                createEvent(
                        primary,
                        "CANCELLED",
                        DAY_START.plusSeconds(6000),
                        DAY_START.plusSeconds(6100),
                        "CANCELLED"
                );

        AcademicCalendarEvent archived =
                createEvent(
                        primary,
                        "ARCHIVED",
                        DAY_START.plusSeconds(7000),
                        DAY_START.plusSeconds(7100),
                        "ARCHIVED"
                );

        AcademicCalendarEvent foreignScheduled =
                createEvent(
                        foreign,
                        "FOREIGN",
                        DAY_START.plusSeconds(1500),
                        DAY_START.plusSeconds(1600),
                        "SCHEDULED"
                );

        List<AcademicCalendarEvent> result =
                queryPrimary();

        assertThat(result)
                .extracting(
                        AcademicCalendarEvent::getId
                )
                .containsExactly(
                        scheduled.getId(),
                        confirmed.getId(),
                        inProgress.getId(),
                        completed.getId()
                );

        assertThat(result)
                .extracting(
                        AcademicCalendarEvent::getId
                )
                .doesNotContain(
                        postponed.getId(),
                        cancelled.getId(),
                        archived.getId(),
                        foreignScheduled.getId()
                );
    }

    private UUID createIsolatedTenant(
            UUID organization,
            String label
    ) {

        UUID tenant =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                VALUES (?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, 0)
                """,
                tenant,
                organization,
                "PROGRAMME-CALENDAR-"
                        + label
                        + "-"
                        + tenant,
                "Programme calendar "
                        + label
                        + " isolation "
                        + tenant
        );

        return tenant;
    }

    private TenantFixture createTenantFixture(
            UUID tenant
    ) {

        authenticate(
                tenant
        );

        return transaction.execute(
                status -> {

                    String suffix =
                            UUID.randomUUID()
                                    .toString();

                    AcademicYear year =
                            years.saveAndFlush(
                                    new AcademicYear(
                                            tenant,
                                            suffix,
                                            "Programme calendar "
                                                    + suffix,
                                            LocalDate.of(
                                                    2035,
                                                    1,
                                                    1
                                            ),
                                            LocalDate.of(
                                                    2035,
                                                    12,
                                                    31
                                            )
                                    )
                            );

                    AcademicTerm term =
                            new AcademicTerm(
                                    year,
                                    "T1",
                                    "Programme test term",
                                    LocalDate.of(
                                            2035,
                                            9,
                                            1
                                    ),
                                    LocalDate.of(
                                            2035,
                                            11,
                                            30
                                    ),
                                    1
                            );

                    term.setTenantId(
                            tenant
                    );

                    term =
                            terms.saveAndFlush(
                                    term
                            );

                    return new TenantFixture(
                            tenant,
                            year,
                            term
                    );
                }
        );
    }

    private AcademicCalendarEvent createEvent(
            TenantFixture fixture,
            String label,
            Instant startAt,
            Instant endAt,
            String targetStatus
    ) {

        authenticate(
                fixture.tenant()
        );

        return transaction.execute(
                status -> {

                    AcademicCalendarEvent event =
                            calendarService.create(
                                    fixture.tenant(),
                                    fixture.year(),
                                    fixture.term(),
                                    label
                                            + "-"
                                            + UUID.randomUUID(),
                                    label,
                                    "STAFF_MEETING",
                                    startAt,
                                    endAt
                            );

                    moveToStatus(
                            event,
                            targetStatus
                    );

                    calendarEvents.flush();

                    return event;
                }
        );
    }

    private void moveToStatus(
            AcademicCalendarEvent event,
            String targetStatus
    ) {

        switch (targetStatus) {

            case "SCHEDULED" -> {
                return;
            }

            case "CONFIRMED" ->
                    event.updateStatus(
                            "CONFIRMED"
                    );

            case "IN_PROGRESS" -> {
                event.updateStatus(
                        "CONFIRMED"
                );
                event.updateStatus(
                        "IN_PROGRESS"
                );
            }

            case "COMPLETED" -> {
                event.updateStatus(
                        "CONFIRMED"
                );
                event.updateStatus(
                        "IN_PROGRESS"
                );
                event.updateStatus(
                        "COMPLETED"
                );
            }

            case "POSTPONED" ->
                    event.updateStatus(
                            "POSTPONED"
                    );

            case "CANCELLED" ->
                    event.updateStatus(
                            "CANCELLED"
                    );

            case "ARCHIVED" -> {
                event.updateStatus(
                        "CANCELLED"
                );
                event.updateStatus(
                        "ARCHIVED"
                );
            }

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported test status: "
                                    + targetStatus
                    );
        }
    }

    private List<AcademicCalendarEvent> queryPrimary() {

        authenticate(
                primary.tenant()
        );

        return transaction.execute(
                status ->
                        programmeCalendar.findCurrentDayEvents(
                                primary.tenant(),
                                DAY_START,
                                DAY_END
                        )
        );
    }

    private void authenticate(
            UUID tenant
    ) {

        RequestContextHolder.set(
                new RequestContext(
                        "teacher-programme-calendar-postgres-test",
                        tenant.toString()
                )
        );

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "teacher-programme-calendar-postgres-test",
                        tenant,
                        Set.of(
                                "SCHOOL_ADMIN"
                        ),
                        Set.of(
                                "school.academic.calendar.read",
                                "school.academic.calendar.create"
                        ),
                        UUID.randomUUID()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of()
                        )
                );
    }
}
