package africa.growtogether.platform.school.teacher.coverage;

import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Authenticated teacher curriculum-coverage view.
 *
 * teachingAssignmentId is null for the all-coverage view and populated
 * when the request is scoped to one owned active teaching assignment.
 */
public record TeacherCoverageView(
        UUID teacherProfileId,
        UUID teachingAssignmentId,
        CoverageSummary summary,
        List<TeacherCoverageResponse> items
) {

    public TeacherCoverageView {
        Objects.requireNonNull(
                teacherProfileId,
                "teacherProfileId must not be null"
        );

        Objects.requireNonNull(
                summary,
                "summary must not be null"
        );

        items = List.copyOf(
                Objects.requireNonNull(
                        items,
                        "items must not be null"
                )
        );
    }

    public record CoverageSummary(
            int total,
            int notStarted,
            int inProgress,
            int completed,
            int requiresRemediation,
            int aheadOfSchedule
    ) {
    }
}
