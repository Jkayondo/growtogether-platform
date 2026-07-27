package africa.growtogether.platform.school.academic.calendar;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/calendar/events")
public class AcademicCalendarEventController {


    private final AcademicCalendarEventService service;

    private final AcademicYearRepository academicYearRepository;

    private final AcademicTermRepository academicTermRepository;

    private final ApiResponses responses;


    public AcademicCalendarEventController(
            AcademicCalendarEventService service,
            AcademicYearRepository academicYearRepository,
            AcademicTermRepository academicTermRepository,
            ApiResponses responses
    ) {
        this.service = service;
        this.academicYearRepository = academicYearRepository;
        this.academicTermRepository = academicTermRepository;
        this.responses = responses;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.calendar.create')")
    public ApiResponse<AcademicCalendarEvent> create(

            @RequestParam UUID tenantId,

            @RequestParam UUID academicYearId,

            @RequestParam(required = false) UUID academicTermId,

            @RequestParam String eventCode,

            @RequestParam String eventName,

            @RequestParam String eventType,

            @RequestParam Instant startAt,

            @RequestParam(required = false) Instant endAt

    ) {


        AcademicYear academicYear =
                academicYearRepository.findById(
                        academicYearId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Academic year not found"
                        )
                );


        AcademicTerm academicTerm = null;


        if (academicTermId != null) {

            academicTerm =
                    academicTermRepository.findById(
                            academicTermId
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Academic term not found"
                            )
                    );
        }


        AcademicCalendarEvent event =
                service.create(
                        tenantId,
                        academicYear,
                        academicTerm,
                        eventCode,
                        eventName,
                        eventType,
                        startAt,
                        endAt
                );


        return responses.success(
                "GT-SCHOOL-CALENDAR-001",
                "Academic calendar event created.",
                event
        );
    }


    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize("hasAuthority('school.academic.calendar.read')")
    public ApiResponse<List<AcademicCalendarEvent>> byAcademicYear(
            @PathVariable UUID academicYearId
    ) {

        return responses.success(
                "GT-SCHOOL-CALENDAR-002",
                "Academic calendar events retrieved.",
                service.findByAcademicYear(
                        academicYearId
                )
        );
    }


    @GetMapping("/academic-term/{academicTermId}")
    @PreAuthorize("hasAuthority('school.academic.calendar.read')")
    public ApiResponse<List<AcademicCalendarEvent>> byAcademicTerm(
            @PathVariable UUID academicTermId
    ) {

        return responses.success(
                "GT-SCHOOL-CALENDAR-003",
                "Academic term calendar events retrieved.",
                service.findByAcademicTerm(
                        academicTermId
                )
        );
    }


    @GetMapping("/upcoming")
    @PreAuthorize("hasAuthority('school.academic.calendar.read')")
    public ApiResponse<List<AcademicCalendarEvent>> upcoming(
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {

        return responses.success(
                "GT-SCHOOL-CALENDAR-004",
                "Upcoming academic calendar events retrieved.",
                service.findUpcomingEvents(
                        start,
                        end
                )
        );
    }

}
