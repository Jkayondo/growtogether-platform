package africa.growtogether.platform.school.teacher.workspace;


import africa.growtogether.platform.school.academic.coverage.TeacherCoverage;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageService;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentService;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEventService;
import africa.growtogether.platform.school.teacher.workspace.api.TeacherWorkspaceResponse;


import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;



@Service
public class TeacherWorkspaceService {


    private final TeacherCoverageService coverageService;

    private final TeachingAssignmentService assignmentService;

    private final AcademicCalendarEventService calendarService;



    public TeacherWorkspaceService(
            TeacherCoverageService coverageService,
            TeachingAssignmentService assignmentService,
            AcademicCalendarEventService calendarService
    ) {

        this.coverageService = coverageService;
        this.assignmentService = assignmentService;
        this.calendarService = calendarService;

    }



    public TeacherWorkspaceResponse getWorkspace(
            UUID tenantId,
            UUID teacherProfileId
    ) {


        List<TeacherCoverage> coverage =
                coverageService.getTeacherCoverage(
                        tenantId,
                        teacherProfileId
                );


        int completed = 0;
        int inProgress = 0;
        int remediation = 0;
        int ahead = 0;


        for (TeacherCoverage item : coverage) {

            switch (item.getCoverageStatus()) {

                case "COMPLETED" ->
                        completed++;

                case "IN_PROGRESS" ->
                        inProgress++;

                case "REQUIRES_REMEDIATION" ->
                        remediation++;

                case "AHEAD_OF_SCHEDULE" ->
                        ahead++;

            }

        }



        TeacherWorkspaceResponse.CoverageSummary coverageSummary =
                new TeacherWorkspaceResponse.CoverageSummary(
                        coverage.size(),
                        completed,
                        inProgress,
                        remediation,
                        ahead
                );



        List<TeachingAssignment> assignments =
                assignmentService.findByTeacher(
                        tenantId,
                        teacherProfileId
                );



        Set<UUID> subjects =
                new HashSet<>();

        Set<UUID> classes =
                new HashSet<>();


        for (TeachingAssignment assignment : assignments) {

            subjects.add(
                    assignment.getSubjectId()
            );


            classes.add(
                    assignment.getClassGradeId()
            );

        }



        TeacherWorkspaceResponse.AssignmentSummary assignmentSummary =
                new TeacherWorkspaceResponse.AssignmentSummary(
                        assignments.size(),
                        subjects.size(),
                        classes.size()
                );




int upcomingEvents =
                calendarService
                        .findScheduledEvents()
                        .size();


        TeacherWorkspaceResponse.CalendarSummary calendarSummary =
                new TeacherWorkspaceResponse.CalendarSummary(
                        upcomingEvents
                );


        return new TeacherWorkspaceResponse(
                teacherProfileId,
                null,
                null,
                assignmentSummary,
                coverageSummary,
                null,
                calendarSummary,
                null
        );

    }


}
