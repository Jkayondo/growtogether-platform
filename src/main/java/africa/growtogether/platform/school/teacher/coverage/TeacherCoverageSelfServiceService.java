package africa.growtogether.platform.school.teacher.coverage;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.connect.ConnectTeacherAuthorizationService;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverage;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageService;
import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageMapper;
import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageResponse;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentService;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TeacherCoverageSelfServiceService {

    private final EnterpriseIdentityContext identity;
    private final ConnectTeacherAuthorizationService teachers;
    private final TeacherCoverageService coverage;
    private final TeachingAssignmentService assignments;
    private final SchoolProfileService schools;
    private final Clock clock;

    @Autowired
    public TeacherCoverageSelfServiceService(
            EnterpriseIdentityContext identity,
            ConnectTeacherAuthorizationService teachers,
            TeacherCoverageService coverage,
            TeachingAssignmentService assignments,
            SchoolProfileService schools
    ) {
        this(
                identity,
                teachers,
                coverage,
                assignments,
                schools,
                Clock.systemUTC()
        );
    }

    TeacherCoverageSelfServiceService(
            EnterpriseIdentityContext identity,
            ConnectTeacherAuthorizationService teachers,
            TeacherCoverageService coverage,
            TeachingAssignmentService assignments,
            SchoolProfileService schools,
            Clock clock
    ) {
        this.identity = Objects.requireNonNull(identity);
        this.teachers = Objects.requireNonNull(teachers);
        this.coverage = Objects.requireNonNull(coverage);
        this.assignments = Objects.requireNonNull(assignments);
        this.schools = Objects.requireNonNull(schools);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional(readOnly = true)
    public TeacherCoverageView currentCoverage() {

        TeacherContext context = currentContext();

        List<TeacherCoverage> items =
                coverage.getTeacherCoverage(
                        context.tenantId(),
                        context.teacherProfileId()
                );

        return toView(
                context.teacherProfileId(),
                null,
                items
        );
    }

    @Transactional(readOnly = true)
    public TeacherCoverageView assignmentCoverage(
            UUID teachingAssignmentId
    ) {

        TeacherContext context = currentContext();

        requireOwnedActiveAssignment(
                context,
                teachingAssignmentId
        );

        List<TeacherCoverage> items =
                coverage.getAssignmentCoverage(
                        context.tenantId(),
                        teachingAssignmentId
                );

        for (TeacherCoverage item : items) {

            if (!context.teacherProfileId().equals(
                    item.getTeacherProfileId()
            )) {
                throw denied();
            }

            if (!teachingAssignmentId.equals(
                    item.getTeachingAssignmentId()
            )) {
                throw denied();
            }
        }

        return toView(
                context.teacherProfileId(),
                teachingAssignmentId,
                items
        );
    }

    @Transactional
    public TeacherCoverageResponse markInProgress(
            UUID coverageId
    ) {

        TeacherContext context = currentContext();

        TeacherCoverage item =
                requireOwnedCoverage(
                        context,
                        coverageId
                );

        requireOwnedActiveAssignment(
                context,
                item.getTeachingAssignmentId()
        );

        item.markInProgress();

        return TeacherCoverageMapper.toResponse(
                coverage.save(item)
        );
    }

    @Transactional
    public TeacherCoverageResponse markCompleted(
            UUID coverageId,
            String remarks
    ) {

        TeacherContext context = currentContext();

        TeacherCoverage item =
                requireOwnedCoverage(
                        context,
                        coverageId
                );

        requireOwnedActiveAssignment(
                context,
                item.getTeachingAssignmentId()
        );

        LocalDate completionDate =
                LocalDate.now(
                        clock.withZone(
                                schools.requireTimezone(
                                        context.tenantId()
                                )
                        )
                );

        item.markCompleted(
                completionDate,
                remarks
        );

        return TeacherCoverageMapper.toResponse(
                coverage.save(item)
        );
    }

    @Transactional
    public TeacherCoverageResponse markRequiresRemediation(
            UUID coverageId
    ) {

        TeacherContext context = currentContext();

        TeacherCoverage item =
                requireOwnedCoverage(
                        context,
                        coverageId
                );

        requireOwnedActiveAssignment(
                context,
                item.getTeachingAssignmentId()
        );

        item.markRequiresRemediation();

        return TeacherCoverageMapper.toResponse(
                coverage.save(item)
        );
    }

    @Transactional
    public TeacherCoverageResponse markAheadOfSchedule(
            UUID coverageId
    ) {

        TeacherContext context = currentContext();

        TeacherCoverage item =
                requireOwnedCoverage(
                        context,
                        coverageId
                );

        requireOwnedActiveAssignment(
                context,
                item.getTeachingAssignmentId()
        );

        item.markAheadOfSchedule();

        return TeacherCoverageMapper.toResponse(
                coverage.save(item)
        );
    }

    private TeacherContext currentContext() {

        UUID tenantId =
                identity.requireTenantId();

        UUID teacherProfileId =
                teachers
                        .requireUniqueCurrentTeacherProfile()
                        .getId();

        return new TeacherContext(
                tenantId,
                teacherProfileId
        );
    }

    private TeacherCoverage requireOwnedCoverage(
            TeacherContext context,
            UUID coverageId
    ) {

        if (coverageId == null) {
            throw denied();
        }

        return coverage
                .getTeacherCoverage(
                        context.tenantId(),
                        context.teacherProfileId()
                )
                .stream()
                .filter(
                        item ->
                                coverageId.equals(
                                        item.getId()
                                )
                )
                .findFirst()
                .orElseThrow(
                        TeacherCoverageSelfServiceService::denied
                );
    }

    private TeachingAssignment requireOwnedActiveAssignment(
            TeacherContext context,
            UUID teachingAssignmentId
    ) {

        if (teachingAssignmentId == null) {
            throw denied();
        }

        final TeachingAssignment assignment;

        try {

            assignment =
                    assignments.findById(
                            context.tenantId(),
                            teachingAssignmentId
                    );

        } catch (IllegalArgumentException exception) {

            throw denied();
        }

        if (!context.teacherProfileId().equals(
                assignment.getTeacherProfileId()
        )) {
            throw denied();
        }

        if (!"ACTIVE".equals(
                assignment.getAssignmentStatus()
        )) {
            throw denied();
        }

        return assignment;
    }

    private TeacherCoverageView toView(
            UUID teacherProfileId,
            UUID teachingAssignmentId,
            List<TeacherCoverage> items
    ) {

        int notStarted = 0;
        int inProgress = 0;
        int completed = 0;
        int remediation = 0;
        int ahead = 0;

        for (TeacherCoverage item : items) {

            switch (item.getCoverageStatus()) {

                case "NOT_STARTED" ->
                        notStarted++;

                case "IN_PROGRESS" ->
                        inProgress++;

                case "COMPLETED" ->
                        completed++;

                case "REQUIRES_REMEDIATION" ->
                        remediation++;

                case "AHEAD_OF_SCHEDULE" ->
                        ahead++;

                default ->
                        throw new IllegalStateException(
                                "Unsupported teacher coverage status: "
                                        + item.getCoverageStatus()
                        );
            }
        }

        List<TeacherCoverageResponse> responses =
                items.stream()
                        .map(
                                TeacherCoverageMapper::toResponse
                        )
                        .toList();

        return new TeacherCoverageView(
                teacherProfileId,
                teachingAssignmentId,
                new TeacherCoverageView.CoverageSummary(
                        items.size(),
                        notStarted,
                        inProgress,
                        completed,
                        remediation,
                        ahead
                ),
                responses
        );
    }

    private static AccessDeniedException denied() {

        return new AccessDeniedException(
                "Teacher coverage access denied."
        );
    }

    private record TeacherContext(
            UUID tenantId,
            UUID teacherProfileId
    ) {
    }
}
